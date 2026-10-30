package com.tazkara.bookingservice.notification;

import com.tazkara.bookingservice.domain.CancellationReason;
import com.tazkara.bookingservice.domain.events.BookingStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationListener {

    @EventListener
    public void onBookingStatusChanged(BookingStatusChangedEvent event) {
        log.info("NOTIFICATION to={} | subject={} | message={}",
                event.attendeeEmail(), subjectFor(event), messageFor(event));
    }

    private String subjectFor(BookingStatusChangedEvent event) {
        return switch (event.status()) {
            case CONFIRMED -> "Booking " + event.reference() + " confirmed";
            case CANCELLED -> "Booking " + event.reference() + " cancelled";
            case EXPIRED -> "Booking " + event.reference() + " expired";
            case PENDING_PAYMENT -> "Booking " + event.reference() + " is waiting for payment";
        };
    }

    private String messageFor(BookingStatusChangedEvent event) {
        String title = event.eventTitle();
        return switch (event.status()) {
            case CONFIRMED -> "Your booking for \"" + title + "\" is confirmed. Your tickets are ready to download.";
            case CANCELLED -> cancelledMessage(event);
            case EXPIRED -> "Your booking for \"" + title + "\" expired because it was not paid in time. "
                    + "The tickets were released.";
            case PENDING_PAYMENT -> "Please complete the payment to keep your tickets for \"" + title + "\".";
        };
    }

    private String cancelledMessage(BookingStatusChangedEvent event) {
        String message = event.cancellationReason() == CancellationReason.EVENT_CANCELLED
                ? "The event \"" + event.eventTitle() + "\" was cancelled, so your booking was cancelled."
                : "Your booking for \"" + event.eventTitle() + "\" was cancelled.";

        if (event.refundAmount() != null) {
            message += " A refund of " + event.refundAmount().toPlainString() + " has been recorded.";
        }
        return message;
    }
}