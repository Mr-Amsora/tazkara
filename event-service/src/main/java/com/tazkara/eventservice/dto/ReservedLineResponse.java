package com.tazkara.eventservice.dto;

import java.math.BigDecimal;

public record ReservedLineResponse(

        Long ticketTypeId,

        String ticketTypeName,

        BigDecimal unitPrice,

        int quantity
) {}