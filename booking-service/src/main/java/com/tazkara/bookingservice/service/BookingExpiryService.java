package com.tazkara.bookingservice.service;

import com.tazkara.bookingservice.client.EventClient;
import com.tazkara.bookingservice.domain.Booking;
import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingExpiryService {

    private static final int BATCH_SIZE = 100;

    private final BookingRepository bookingRepository;
    private final EventClient eventClient;

    public int expireOverdueBookings() {
        List<Booking> overdue = bookingRepository.findByStatusAndHoldExpiresAtBefore(
                BookingStatus.PENDING_PAYMENT, Instant.now(), PageRequest.of(0, BATCH_SIZE));

        int expired = 0;
        for (Booking booking : overdue) {
            try {
                booking.expire();
                bookingRepository.save(booking);
                expired++;
            } catch (ObjectOptimisticLockingFailureException e) {
                log.info("Booking {} was changed at the same moment, skipping expiry", booking.getReference());
            }
        }
        return expired;
    }

    public int releaseTickets() {
        List<Booking> bookings = bookingRepository.findByStatusInAndTicketsReleasedFalse(
                BookingStatus.RELEASING, PageRequest.of(0, BATCH_SIZE));

        int released = 0;
        for (Booking booking : bookings) {
            List<EventClient.Line> lines = booking.getLines().stream()
                    .map(line -> new EventClient.Line(line.getTicketTypeId(), line.getQuantity()))
                    .toList();

            if (eventClient.release(booking.getEventId(), lines)) {
                booking.markTicketsReleased();
                bookingRepository.save(booking);
                released++;
            }
        }
        return released;
    }
}