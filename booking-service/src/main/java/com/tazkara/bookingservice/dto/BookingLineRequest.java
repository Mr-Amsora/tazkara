package com.tazkara.bookingservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BookingLineRequest(
        @NotNull Long ticketTypeId,
        @NotNull @Min(1) Integer quantity
) {}