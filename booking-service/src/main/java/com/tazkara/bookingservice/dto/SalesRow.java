package com.tazkara.bookingservice.dto;

import com.tazkara.bookingservice.domain.BookingStatus;

import java.math.BigDecimal;

public record SalesRow(Long ticketTypeId, BookingStatus status, Long tickets, BigDecimal revenue) {}