package com.tazkara.bookingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.booking")
public record BookingProperties(int holdMinutes, int maxTicketsPerEvent) {}