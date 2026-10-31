package com.tazkara.bookingservice.notification;

import com.tazkara.bookingservice.domain.events.BookingStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuditLogListener {

    @EventListener
    public void onBookingStatusChanged(BookingStatusChangedEvent event) {
        log.info("AUDIT booking {} is now {}", event.reference(), event.status());
    }
}