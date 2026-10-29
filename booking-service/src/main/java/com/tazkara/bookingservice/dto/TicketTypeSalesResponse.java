package com.tazkara.bookingservice.dto;

import java.math.BigDecimal;

public record TicketTypeSalesResponse(
        Long ticketTypeId,
        String ticketTypeName,
        int total,
        long sold,
        long held,
        long remaining,
        BigDecimal revenue
) {}