package com.tazkara.bookingservice.dto;

import com.tazkara.bookingservice.domain.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PayBookingRequest(@NotNull PaymentMethod paymentMethod) {}