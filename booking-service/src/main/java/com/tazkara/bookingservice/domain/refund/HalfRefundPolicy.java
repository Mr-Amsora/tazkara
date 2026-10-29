package com.tazkara.bookingservice.domain.refund;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HalfRefundPolicy implements RefundPolicy {

    private static final BigDecimal HALF = new BigDecimal("0.50");

    @Override
    public boolean allowsCancellation() {
        return true;
    }

    @Override
    public BigDecimal refundFor(BigDecimal amountPaid) {
        return amountPaid.multiply(HALF).setScale(2, RoundingMode.HALF_UP);
    }
}