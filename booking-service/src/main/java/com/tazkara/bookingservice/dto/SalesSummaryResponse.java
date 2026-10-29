package com.tazkara.bookingservice.dto;

import java.util.List;

public record SalesSummaryResponse(Long eventId, String eventTitle, List<TicketTypeSalesResponse> ticketTypes) {}