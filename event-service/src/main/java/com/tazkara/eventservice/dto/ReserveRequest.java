package com.tazkara.eventservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReserveRequest(

        @NotEmpty @Valid List<Line> lines
) {
    public record Line(

            @NotNull Long ticketTypeId,

            @Min(1) int quantity
    ) {}
}