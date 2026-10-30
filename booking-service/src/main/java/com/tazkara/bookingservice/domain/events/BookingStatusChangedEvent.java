package com.tazkara.bookingservice.domain.events;

import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.domain.CancellationReason;

import java.math.BigDecimal;

public record BookingStatusChangedEvent(
        String reference,
        String attendeeEmail,
        String eventTitle,
        BookingStatus status,
        CancellationReason cancellationReason,
        BigDecimal refundAmount
) {

    public static BookingStatusChangedEvent from(Booking booking) {
        return new BookingStatusChangedEvent(
                booking.getReference(),
                booking.getAttendeeEmail(),
                booking.getEventTitle(),
                booking.getStatus(),
                booking.getCancellationReason(),
                booking.getRefundAmount());
    }
}