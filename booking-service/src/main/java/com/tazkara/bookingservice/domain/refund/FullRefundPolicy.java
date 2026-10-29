package com.tazkara.bookingservice.domain.refund;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FullRefundPolicy implements RefundPolicy {

    @Override
    public boolean allowsCancellation() {
        return true;
    }

    @Override
    public BigDecimal refundFor(BigDecimal amountPaid) {
        return amountPaid.setScale(2, RoundingMode.HALF_UP);
    }
}