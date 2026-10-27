package com.tazkara.eventservice.dto;

import com.tazkara.eventservice.domain.EventStatus;

import java.time.Instant;

public record EventInfoResponse(

        Long id,

        Long organizerId,

        String title,

        EventStatus status,

        Instant startTime
) {}