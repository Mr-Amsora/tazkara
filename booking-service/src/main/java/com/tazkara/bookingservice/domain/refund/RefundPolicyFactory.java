package com.tazkara.bookingservice.domain.refund;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefundPolicyFactory {

    private static final Duration FULL_REFUND_NOTICE = Duration.ofDays(7);
    private static final Duration HALF_REFUND_NOTICE = Duration.ofHours(48);

    private final RefundPolicy fullRefund = new FullRefundPolicy();
    private final RefundPolicy halfRefund = new HalfRefundPolicy();
    private final RefundPolicy noRefund = new NoRefundPolicy();

    public RefundPolicy forTimeLeft(Duration timeLeft) {
        if (timeLeft.compareTo(FULL_REFUND_NOTICE) >= 0) {
            return fullRefund;
        }
        if (timeLeft.compareTo(HALF_REFUND_NOTICE) >= 0) {
            return halfRefund;
        }
        return noRefund;
    }
}