package com.tazkara.bookingservice.service;

import com.tazkara.bookingservice.client.EventClient;
import com.tazkara.bookingservice.config.BookingProperties;
import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingLine;
import com.tazkara.bookingservice.domain.BookingReference;
import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.domain.refund.RefundPolicy;
import com.tazkara.bookingservice.domain.refund.RefundPolicyFactory;
import com.tazkara.bookingservice.dto.BookingLineRequest;
import com.tazkara.bookingservice.dto.BookingResponse;
import com.tazkara.bookingservice.dto.BookingSummaryResponse;
import com.tazkara.bookingservice.dto.PageResponse;
import com.tazkara.bookingservice.dto.PayBookingRequest;
import com.tazkara.bookingservice.dto.PlaceBookingRequest;
import com.tazkara.bookingservice.exception.BookingException;
import com.tazkara.bookingservice.mapper.BookingMapper;
import com.tazkara.bookingservice.repository.BookingRepository;
import com.tazkara.bookingservice.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

    private final BookingRepository bookingRepository;
    private final EventClient eventClient;
    private final BookingProperties properties;
    private final RefundPolicyFactory refundPolicyFactory;

    public BookingResponse placeBooking(AuthenticatedUser user, PlaceBookingRequest request) {
        Instant now = Instant.now();

        EventClient.EventInfo event = eventClient.getEvent(request.eventId());
        requireOpenEvent(event, now);

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

    public BookingResponse payBooking(AuthenticatedUser user, String reference, PayBookingRequest request) {
        Booking booking = findOwned(user, reference);

        Instant now = Instant.now();
        booking.ensurePayable(now);
        requireOpenEvent(eventClient.getEvent(booking.getEventId()), now);

        booking.confirm(request.paymentMethod(), now);
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    public BookingResponse cancelBooking(AuthenticatedUser user, String reference) {
        Booking booking = findOwned(user, reference);

        Duration timeLeft = Duration.between(Instant.now(), booking.getEventStartTime());
        RefundPolicy policy = refundPolicyFactory.forTimeLeft(timeLeft);

        booking.cancelByAttendee(policy);
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    public BookingResponse getBooking(AuthenticatedUser user, String reference) {
        return BookingMapper.toResponse(findOwned(user, reference));
    }

    public PageResponse<BookingSummaryResponse> listMyBookings(AuthenticatedUser user,
                                                               BookingStatus status,
                                                               Pageable pageable) {
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), NEWEST_FIRST);

        Page<Booking> page = (status == null)
                ? bookingRepository.findByAttendeeId(user.id(), sorted)
                : bookingRepository.findByAttendeeIdAndStatus(user.id(), status, sorted);

        return PageResponse.from(page.map(BookingMapper::toSummary));
    }

    private Booking findOwned(AuthenticatedUser user, String reference) {
        return bookingRepository.findByReferenceAndAttendeeId(reference, user.id())
                .orElseThrow(() -> new BookingException(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND",
                        "Booking not found"));
    }

    private void requireOpenEvent(EventClient.EventInfo event, Instant now) {
        if (!"PUBLISHED".equals(event.status()) || !event.startTime().isAfter(now)) {
            throw new BookingException(HttpStatus.UNPROCESSABLE_ENTITY, "EVENT_NOT_BOOKABLE",
                    "This event is not open for booking");
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