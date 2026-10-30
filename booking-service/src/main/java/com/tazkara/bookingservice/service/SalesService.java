package com.tazkara.bookingservice.service;

import com.tazkara.bookingservice.client.EventClient;
import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.dto.FileDownload;
import com.tazkara.bookingservice.dto.OrganizerBookingResponse;
import com.tazkara.bookingservice.dto.PageResponse;
import com.tazkara.bookingservice.dto.SalesSummaryResponse;
import com.tazkara.bookingservice.dto.TicketTypeSalesResponse;
import com.tazkara.bookingservice.exception.BookingException;
import com.tazkara.bookingservice.export.BookingCsvExporter;
import com.tazkara.bookingservice.mapper.BookingMapper;
import com.tazkara.bookingservice.repository.BookingRepository;
import com.tazkara.bookingservice.repository.SalesRow;
import com.tazkara.bookingservice.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesService {

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");
    private static final BigDecimal NO_REVENUE = BigDecimal.ZERO.setScale(2);

    private final BookingRepository bookingRepository;
    private final EventClient eventClient;

    public PageResponse<OrganizerBookingResponse> listEventBookings(AuthenticatedUser organizer,
                                                                    Long eventId,
                                                                    BookingStatus status,
                                                                    Pageable pageable) {
        requireOwnedEvent(organizer, eventId);

        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), NEWEST_FIRST);
        Page<Booking> page = (status == null)
                ? bookingRepository.findByEventId(eventId, sorted)
                : bookingRepository.findByEventIdAndStatus(eventId, status, sorted);

        return PageResponse.from(page.map(BookingMapper::toOrganizerResponse));
    }

    public SalesSummaryResponse getSales(AuthenticatedUser organizer, Long eventId) {
        EventClient.EventInfo event = requireOwnedEvent(organizer, eventId);

        List<SalesRow> rows = bookingRepository.summarizeSales(
                eventId, List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT));

        List<TicketTypeSalesResponse> ticketTypes = event.ticketTypes().stream()
                .map(type -> toSales(type, rows))
                .toList();

        return new SalesSummaryResponse(event.id(), event.title(), ticketTypes);
    }

    public FileDownload exportConfirmedBookings(AuthenticatedUser organizer, Long eventId) {
        requireOwnedEvent(organizer, eventId);

        List<Booking> confirmed = bookingRepository
                .findByEventIdAndStatusOrderByPaidAtAsc(eventId, BookingStatus.CONFIRMED);

        String fileName = "bookings-event-" + eventId + "-" + LocalDate.now(ZoneOffset.UTC) + ".csv";
        return new FileDownload(fileName, BookingCsvExporter.export(confirmed));
    }

    private TicketTypeSalesResponse toSales(EventClient.TicketTypeInfo type, List<SalesRow> rows) {
        long sold = ticketsOf(rows, type.id(), BookingStatus.CONFIRMED);
        long held = ticketsOf(rows, type.id(), BookingStatus.PENDING_PAYMENT);
        BigDecimal revenue = rows.stream()
                .filter(row -> row.ticketTypeId().equals(type.id()) && row.status() == BookingStatus.CONFIRMED)
                .map(SalesRow::revenue)
                .findFirst()
                .orElse(NO_REVENUE);

        long remaining = Math.max(0, type.quantity() - sold - held);
        return new TicketTypeSalesResponse(type.id(), type.name(), type.quantity(), sold, held, remaining, revenue);
    }

    private long ticketsOf(List<SalesRow> rows, Long ticketTypeId, BookingStatus status) {
        return rows.stream()
                .filter(row -> row.ticketTypeId().equals(ticketTypeId) && row.status() == status)
                .mapToLong(SalesRow::tickets)
                .sum();
    }

    private EventClient.EventInfo requireOwnedEvent(AuthenticatedUser organizer, Long eventId) {
        EventClient.EventInfo event = eventClient.getEvent(eventId);
        if (!event.organizerId().equals(organizer.id())) {
            throw new BookingException(HttpStatus.FORBIDDEN, "NOT_EVENT_OWNER",
                    "You can only view sales of your own events");
        }
        return event;
    }
}