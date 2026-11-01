package com.tazkara.bookingservice.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(int holdMinutes, int maxTicketsPerEvent) {}