package com.tazkara.eventservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record UpdateEventRequest(

        @Size(max = 200)
        String title,

        @Size(max = 4000)
        String description,

        @Size(max = 200)
        String venue,

        @Size(max = 100)
        String city,

        Instant startTime,

        @Size(min = 1, max = 5) @Valid
        List<TicketTypeRequest> ticketTypes
) {}