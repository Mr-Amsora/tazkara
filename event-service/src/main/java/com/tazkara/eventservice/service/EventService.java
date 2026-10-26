package com.tazkara.eventservice.service;

import com.tazkara.eventservice.domain.Event;
import com.tazkara.eventservice.domain.TicketType;
import com.tazkara.eventservice.dto.CreateEventRequest;
import com.tazkara.eventservice.dto.EventResponse;
import com.tazkara.eventservice.mapper.EventMapper;
import com.tazkara.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventResponse createEvent(Long organizerId, CreateEventRequest request) {
        List<TicketType> ticketTypes = request.ticketTypes().stream()
                .map(type -> new TicketType(type.name().trim(), type.price(), type.quantity()))
                .toList();

        Event event = Event.create(
                organizerId,
                request.title().trim(),
                request.description().trim(),
                request.venue().trim(),
                request.city().trim(),
                request.startTime(),
                ticketTypes,
                Instant.now());

        return EventMapper.toResponse(eventRepository.save(event));
    }
}