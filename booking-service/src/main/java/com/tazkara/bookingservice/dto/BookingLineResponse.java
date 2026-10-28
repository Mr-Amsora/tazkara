package com.tazkara.bookingservice.dto;

import java.math.BigDecimal;

public record BookingLineResponse(
        Long ticketTypeId,
        String ticketTypeName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {}