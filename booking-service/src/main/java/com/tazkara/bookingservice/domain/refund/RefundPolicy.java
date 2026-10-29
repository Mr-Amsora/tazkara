package com.tazkara.bookingservice.domain.refund;

import java.math.BigDecimal;

public interface RefundPolicy {

    boolean allowsCancellation();

    BigDecimal refundFor(BigDecimal amountPaid);
}