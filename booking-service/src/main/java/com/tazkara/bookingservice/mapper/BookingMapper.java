package com.tazkara.bookingservice.mapper;

import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.dto.BookingLineResponse;
import com.tazkara.bookingservice.dto.BookingResponse;

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
                booking.getHoldExpiresAt(),
                booking.getPaidAt(),
                booking.getCancellationReason(),
                booking.getRefundAmount(),
                lines,
                booking.getCreatedAt());
    }
}