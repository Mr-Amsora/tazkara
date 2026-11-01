package com.tazkara.bookingservice.domain.refund;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefundPolicyFactory {

    private static final Duration FULL_REFUND_DURATION = Duration.ofDays(7);
    private static final Duration HALF_REFUND_DURATION = Duration.ofHours(48);

    public RefundPolicy forTimeLeft(Duration timeLeft) {
        if (timeLeft.compareTo(FULL_REFUND_DURATION) >= 0) {
            return new FullRefundPolicy();
        }
        if (timeLeft.compareTo(HALF_REFUND_DURATION) >= 0) {
            return new HalfRefundPolicy();
        }
        return new NoRefundPolicy();
    }

    public RefundPolicy forEventCancellation() {
        return new FullRefundPolicy();
    }
}