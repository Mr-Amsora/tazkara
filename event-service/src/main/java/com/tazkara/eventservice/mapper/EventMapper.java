package com.tazkara.eventservice.mapper;

import com.tazkara.eventservice.domain.Event;
import com.tazkara.eventservice.dto.EventResponse;
import com.tazkara.eventservice.dto.TicketTypeResponse;

import java.util.List;

public class EventMapper {

    private EventMapper() {}

    public static EventResponse toResponse(Event event) {
        List<TicketTypeResponse> ticketTypes = event.getTicketTypes().stream()
                .map(type -> new TicketTypeResponse(
                        type.getId(), type.getName(), type.getPrice(), type.getQuantity()))
                .toList();

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue(),
                event.getCity(),
                event.getStartTime(),
                event.getStatus(),
                ticketTypes);
    }
}