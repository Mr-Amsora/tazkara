package com.tazkara.bookingservice.service;

import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.domain.events.BookingStatusChangedEvent;
import com.tazkara.bookingservice.domain.refund.RefundPolicy;
import com.tazkara.bookingservice.domain.refund.RefundPolicyFactory;
import com.tazkara.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventCancellationService {

    private final BookingRepository bookingRepository;
    private final RefundPolicyFactory refundPolicyFactory;
    private final ApplicationEventPublisher eventPublisher;

    public void cancelBookingsOfEvent(Long eventId) {
        RefundPolicy policy = refundPolicyFactory.forEventCancellation();
        List<Booking> activeBookings = bookingRepository.findByEventIdAndStatusIn(eventId, BookingStatus.ACTIVE);

        int cancelled = 0;
        int conflicts = 0;

        for (Booking booking : activeBookings) {
            try {
                booking.cancelByEvent(policy);
                Booking saved = bookingRepository.save(booking);
                eventPublisher.publishEvent(BookingStatusChangedEvent.from(saved));
                cancelled++;
            } catch (ObjectOptimisticLockingFailureException e) {
                conflicts++;
                log.warn("Booking {} changed while cancelling event {}", booking.getReference(), eventId);
            }
        }

        log.info("Event {} cancelled: {} bookings cancelled, {} need another attempt",
                eventId, cancelled, conflicts);

        if (conflicts > 0) {
            throw new IllegalStateException("Some bookings of event " + eventId + " changed concurrently");
        }
    }
}