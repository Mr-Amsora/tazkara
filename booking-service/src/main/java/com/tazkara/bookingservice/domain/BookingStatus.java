package com.tazkara.bookingservice.domain;

import java.util.List;

public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    EXPIRED,
    CANCELLED;

    public static final List<BookingStatus> ACTIVE = List.of(PENDING_PAYMENT, CONFIRMED);

    public static final List<BookingStatus> RELEASING = List.of(EXPIRED, CANCELLED);

    public boolean canMoveTo(BookingStatus next) {
        if (this == PENDING_PAYMENT) {
            if (next == CONFIRMED || next == EXPIRED || next == CANCELLED) {
                return true;
            }
            return false;
        }
        if (this == CONFIRMED) {
            if (next == CANCELLED) {
                return true;
            }
            return false;
        }
        return false;
    }
}