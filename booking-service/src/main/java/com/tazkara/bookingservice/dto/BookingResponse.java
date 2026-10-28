package com.tazkara.bookingservice.dto;

import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.domain.CancellationReason;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long id,
        String reference,
        Long eventId,
        String eventTitle,
        Instant eventStartTime,
        BookingStatus status,
        BigDecimal totalAmount,
        Instant holdExpiresAt,
        Instant paidAt,
        CancellationReason cancellationReason,
        BigDecimal refundAmount,
        List<BookingLineResponse> lines,
        Instant createdAt
) {}