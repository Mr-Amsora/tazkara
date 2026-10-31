package com.tazkara.eventservice.service;

import com.tazkara.eventservice.domain.Event;
import com.tazkara.eventservice.domain.EventStatus;
import com.tazkara.eventservice.domain.TicketType;
import com.tazkara.eventservice.dto.ReserveRequest;
import com.tazkara.eventservice.dto.ReservedLineResponse;
import com.tazkara.eventservice.exception.EventNotFoundException;
import com.tazkara.eventservice.exception.InvalidEventException;
import com.tazkara.eventservice.repository.EventRepository;
import com.tazkara.eventservice.repository.TicketTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class InventoryService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    @Transactional
    public List<ReservedLineResponse> reserve(Long eventId, ReserveRequest request) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getStatus() != EventStatus.PUBLISHED
                || !event.getStartTime().isAfter(Instant.now())) {
            throw new InvalidEventException("Event is not open for booking");
        }

        Map<Long, TicketType> ticketTypes = lockTicketTypes(eventId, request);

        List<ReservedLineResponse> reserved = new ArrayList<>();

        for (ReserveRequest.Line line : request.lines()) {
            TicketType ticketType = ticketTypes.get(line.ticketTypeId());

            ticketType.reserve(line.quantity());

            reserved.add(new ReservedLineResponse(
                    ticketType.getId(),
                    ticketType.getName(),
                    ticketType.getPrice(),
                    line.quantity()
            ));
        }

        return reserved;
    }

    @Transactional
    public void release(Long eventId, ReserveRequest request) {

        Map<Long, TicketType> ticketTypes = lockTicketTypes(eventId, request);

        for (ReserveRequest.Line line : request.lines()) {
            ticketTypes.get(line.ticketTypeId()).release(line.quantity());
        }
    }

    private Map<Long, TicketType> lockTicketTypes(Long eventId, ReserveRequest request) {

        List<Long> ids = request.lines().stream()
                .map(ReserveRequest.Line::ticketTypeId)
                .distinct()
                .toList();

        List<TicketType> types =
                ticketTypeRepository.findByEventIdAndIdInOrderById(eventId, ids);

        Map<Long, TicketType> ticketTypes = new HashMap<>();

        for (TicketType type : types) {
            ticketTypes.put(type.getId(), type);
        }

        if (ticketTypes.size() != ids.size()) {
            throw new InvalidEventException("A ticket type does not belong to this event");
        }

        return ticketTypes;
    }
}
