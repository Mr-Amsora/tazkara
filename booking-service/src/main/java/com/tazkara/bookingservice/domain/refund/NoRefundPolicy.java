package com.tazkara.bookingservice.domain.refund;

import java.math.BigDecimal;

public class NoRefundPolicy implements RefundPolicy {

    @Override
    public boolean allowsCancellation() {
        return false;
    }

    @Override
    public BigDecimal refundFor(BigDecimal amountPaid) {
        return BigDecimal.ZERO.setScale(2);
    }
}