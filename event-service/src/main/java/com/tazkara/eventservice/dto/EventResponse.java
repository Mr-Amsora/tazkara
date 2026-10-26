package com.tazkara.eventservice.dto;

import com.tazkara.eventservice.domain.EventStatus;

import java.time.Instant;
import java.util.List;

public record EventResponse(

        Long id,

        String title,

        String description,

        String venue,

        String city,

        Instant startTime,

        EventStatus status,

        List<TicketTypeResponse> ticketTypes
) {}