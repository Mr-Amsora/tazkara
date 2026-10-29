package com.tazkara.bookingservice.scheduler;

import com.tazkara.bookingservice.service.BookingExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryJob {

    private final BookingExpiryService expiryService;

    @Scheduled(fixedDelayString = "${app.booking.expiry-job-interval-ms}")
    public void run() {
        int expired = expiryService.expireOverdueBookings();
        int released = expiryService.releaseTickets();

        if (expired > 0 || released > 0) {
            log.info("Expiry job on thread {}: {} bookings expired, {} bookings had their tickets released",
                    Thread.currentThread().getName(), expired, released);
        }
    }
}