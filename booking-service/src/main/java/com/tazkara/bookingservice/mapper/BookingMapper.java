package com.tazkara.bookingservice.mapper;

import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingLine;
import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.dto.BookingLineResponse;
import com.tazkara.bookingservice.dto.BookingResponse;
import com.tazkara.bookingservice.dto.BookingSummaryResponse;
import com.tazkara.bookingservice.dto.OrganizerBookingResponse;

import java.time.Instant;
import java.util.List;

public final class BookingMapper {

    private BookingMapper() {}

    public static BookingResponse toResponse(Booking booking) {
        List<BookingLineResponse> lines = booking.getLines().stream()
                .map(line -> new BookingLineResponse(
                        line.getTicketTypeId(),
                        line.getTicketTypeName(),
                        line.getUnitPrice(),
                        line.getQuantity(),
                        line.lineTotal()))
                .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getReference(),
                booking.getEventId(),
                booking.getEventTitle(),
                booking.getEventStartTime(),
                booking.getStatus(),
                booking.getTotalAmount(),
                holdExpiryIfPending(booking),
                booking.getPaidAt(),
                booking.getCancellationReason(),
                booking.getRefundAmount(),
                lines,
                booking.getCreatedAt());
    }

    public static BookingSummaryResponse toSummary(Booking booking) {
        return new BookingSummaryResponse(
                booking.getReference(),
                booking.getEventId(),
                booking.getEventTitle(),
                booking.getEventStartTime(),
                booking.getStatus(),
                ticketCount(booking),
                booking.getTotalAmount(),
                holdExpiryIfPending(booking),
                booking.getCreatedAt());
    }

    public static OrganizerBookingResponse toOrganizerResponse(Booking booking) {
        List<OrganizerBookingResponse.Ticket> tickets = booking.getLines().stream()
                .map(line -> new OrganizerBookingResponse.Ticket(line.getTicketTypeName(), line.getQuantity()))
                .toList();

        return new OrganizerBookingResponse(
                booking.getReference(),
                booking.getAttendeeEmail(),
                booking.getStatus(),
                ticketCount(booking),
                tickets,
                booking.getTotalAmount(),
                booking.getCreatedAt());
    }

    private static Instant holdExpiryIfPending(Booking booking) {
        return booking.getStatus() == BookingStatus.PENDING_PAYMENT ? booking.getHoldExpiresAt() : null;
    }

    private static int ticketCount(Booking booking) {
        return booking.getLines().stream().mapToInt(BookingLine::getQuantity).sum();
    }
}