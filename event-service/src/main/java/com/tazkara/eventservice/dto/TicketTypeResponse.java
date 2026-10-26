package com.tazkara.eventservice.dto;

import java.math.BigDecimal;

public record TicketTypeResponse(

        Long id,

        String name,

        BigDecimal price,

        int quantity,

        int availableTickets,

        boolean soldOut
) {}