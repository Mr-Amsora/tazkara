package com.tazkara.bookingservice.dto;

import com.tazkara.bookingservice.domain.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record BookingSummaryResponse(
        String reference,
        Long eventId,
        String eventTitle,
        Instant eventStartTime,
        BookingStatus status,
        int ticketCount,
        BigDecimal totalAmount,
        Instant holdExpiresAt,
        Instant createdAt
) {}