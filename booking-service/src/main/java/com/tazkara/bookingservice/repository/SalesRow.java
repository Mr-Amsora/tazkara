package com.tazkara.bookingservice.repository;

import com.tazkara.bookingservice.domain.BookingStatus;

import java.math.BigDecimal;

public record SalesRow(Long ticketTypeId, BookingStatus status, Long tickets, BigDecimal revenue) {}