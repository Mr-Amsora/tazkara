package com.tazkara.bookingservice.dto;

import com.tazkara.bookingservice.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrganizerBookingResponse(
        String reference,
        String attendeeEmail,
        BookingStatus status,
        int ticketCount,
        List<Ticket> tickets,
        BigDecimal totalAmount,
        Instant createdAt
) {

    public record Ticket(String ticketTypeName, int quantity) {}
}