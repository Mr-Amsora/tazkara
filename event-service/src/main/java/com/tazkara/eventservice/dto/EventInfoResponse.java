package com.tazkara.eventservice.dto;

import com.tazkara.eventservice.domain.EventStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record EventInfoResponse(
        Long id,
        Long organizerId,
        String title,
        EventStatus status,
        Instant startTime,
        List<TicketTypeInfo> ticketTypes
) {

    public record TicketTypeInfo(Long id, String name, BigDecimal price, int quantity) {}
}