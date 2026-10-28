package com.tazkara.bookingservice.service;

import com.tazkara.bookingservice.client.EventClient;
import com.tazkara.bookingservice.config.BookingProperties;
import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingLine;
import com.tazkara.bookingservice.domain.BookingReference;
import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.dto.BookingLineRequest;
import com.tazkara.bookingservice.dto.BookingResponse;
import com.tazkara.bookingservice.dto.PlaceBookingRequest;
import com.tazkara.bookingservice.exception.BookingException;
import com.tazkara.bookingservice.mapper.BookingMapper;
import com.tazkara.bookingservice.repository.BookingRepository;
import com.tazkara.bookingservice.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventClient eventClient;
    private final BookingProperties properties;

    public BookingResponse placeBooking(AuthenticatedUser user, PlaceBookingRequest request) {
        Instant now = Instant.now();

        EventClient.EventInfo event = eventClient.getEvent(request.eventId());
        if (!"PUBLISHED".equals(event.status()) || !event.startTime().isAfter(now)) {
            throw new BookingException(HttpStatus.UNPROCESSABLE_ENTITY, "EVENT_NOT_BOOKABLE",
                    "This event is not open for booking");
        }

        checkNoDuplicateTicketTypes(request);
        checkTicketLimit(user.id(), event.id(), request);

        List<EventClient.Line> requestedLines = request.lines().stream()
                .map(line -> new EventClient.Line(line.ticketTypeId(), line.quantity()))
                .toList();

        List<BookingLine> lines = eventClient.reserve(event.id(), requestedLines).stream()
                .map(reserved -> new BookingLine(
                        reserved.ticketTypeId(), reserved.ticketTypeName(),
                        reserved.unitPrice(), reserved.quantity()))
                .toList();

        Booking booking = Booking.builder()
                .attendeeId(user.id())
                .attendeeEmail(user.email())
                .eventId(event.id())
                .eventTitle(event.title())
                .eventStartTime(event.startTime())
                .reference(BookingReference.generate())
                .lines(lines)
                .holdExpiresAt(now.plus(properties.holdMinutes(), ChronoUnit.MINUTES))
                .now(now)
                .build();

        try {
            return BookingMapper.toResponse(bookingRepository.save(booking));
        } catch (RuntimeException e) {
            eventClient.release(event.id(), requestedLines);
            throw e;
        }
    }

    private void checkNoDuplicateTicketTypes(PlaceBookingRequest request) {
        long distinctTypes = request.lines().stream()
                .map(BookingLineRequest::ticketTypeId)
                .distinct()
                .count();
        if (distinctTypes != request.lines().size()) {
            throw new BookingException(HttpStatus.BAD_REQUEST, "DUPLICATE_TICKET_TYPE",
                    "Each ticket type can appear only once in a booking");
        }
    }

    private void checkTicketLimit(Long attendeeId, Long eventId, PlaceBookingRequest request) {
        int requested = request.lines().stream().mapToInt(BookingLineRequest::quantity).sum();
        long active = bookingRepository.countActiveTickets(attendeeId, eventId, BookingStatus.ACTIVE);

        if (active + requested > properties.maxTicketsPerEvent()) {
            throw new BookingException(HttpStatus.UNPROCESSABLE_ENTITY, "TICKET_LIMIT_EXCEEDED",
                    "You can hold at most " + properties.maxTicketsPerEvent() + " tickets per event");
        }
    }
}