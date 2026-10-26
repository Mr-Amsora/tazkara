package com.tazkara.eventservice.service;

import com.tazkara.eventservice.domain.Event;
import com.tazkara.eventservice.domain.EventStatus;
import com.tazkara.eventservice.domain.TicketType;
import com.tazkara.eventservice.dto.CreateEventRequest;
import com.tazkara.eventservice.dto.EventResponse;
import com.tazkara.eventservice.dto.UpdateEventRequest;
import com.tazkara.eventservice.exception.EventAccessDeniedException;
import com.tazkara.eventservice.exception.EventNotFoundException;
import com.tazkara.eventservice.exception.InvalidEventException;
import com.tazkara.eventservice.mapper.EventMapper;
import com.tazkara.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public EventResponse updateEvent(Long organizerId, Long eventId, UpdateEventRequest request) {
        Event event = findEvent(eventId);

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new EventAccessDeniedException("You do not have permission to edit this event");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new InvalidEventException("Only draft events can be edited");
        }

        if (request.title() != null && !request.title().trim().isEmpty()) {
            event.setTitle(request.title().trim());
        }

        if (request.description() != null && !request.description().trim().isEmpty()) {
            event.setDescription(request.description().trim());
        }

        if (request.venue() != null && !request.venue().trim().isEmpty()) {
            event.setVenue(request.venue().trim());
        }

        if (request.city() != null && !request.city().trim().isEmpty()) {
            event.setCity(request.city().trim());
        }

        if (request.startTime() != null) {
            Event.validateStartTime(request.startTime(), Instant.now());
            event.setStartTime(request.startTime());
        }

        if (request.ticketTypes() != null) {
            List<TicketType> newTicketTypes = request.ticketTypes().stream()
                    .map(type -> new TicketType(type.name().trim(), type.price(), type.quantity()))
                    .toList();
            Event.validateTicketTypes(newTicketTypes);

            event.getTicketTypes().clear();
            for (TicketType ticketType : newTicketTypes) {
                ticketType.attachTo(event);
                event.getTicketTypes().add(ticketType);
            }
        }

        return EventMapper.toResponse(event);
    }

    @Transactional
    public EventResponse publishEvent(Long organizerId, Long eventId) {
        Event event = findEvent(eventId);

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new EventAccessDeniedException("You do not have permission to publish this event");
        }

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new InvalidEventException("Only draft events can be published");
        }

        Event.validateStartTime(event.getStartTime(), Instant.now());
        event.setStatus(EventStatus.PUBLISHED);

        return EventMapper.toResponse(event);
    }

    private Event findEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with id: " + eventId));
    }
}