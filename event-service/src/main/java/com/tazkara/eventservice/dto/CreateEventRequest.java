package com.tazkara.eventservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record CreateEventRequest(

        @NotBlank @Size(max = 200)
        String title,

        @NotBlank @Size(max = 4000)
        String description,

        @NotBlank @Size(max = 200)
        String venue,

        @NotBlank @Size(max = 100)
        String city,

        @NotNull
        Instant startTime,

        @NotNull @Size(min = 1, max = 5) @Valid
        List<TicketTypeRequest> ticketTypes
) {}