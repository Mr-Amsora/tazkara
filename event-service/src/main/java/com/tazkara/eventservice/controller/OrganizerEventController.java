package com.tazkara.eventservice.controller;

import com.tazkara.eventservice.dto.CreateEventRequest;
import com.tazkara.eventservice.dto.EventResponse;
import com.tazkara.eventservice.dto.UpdateEventRequest;
import com.tazkara.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizer/events")
@RequiredArgsConstructor
public class OrganizerEventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@AuthenticationPrincipal Long organizerId,
                                                     @Valid @RequestBody CreateEventRequest request) {
        EventResponse response = eventService.createEvent(organizerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EventResponse> updateEvent(@AuthenticationPrincipal Long organizerId,
                                                     @PathVariable Long id,
                                                     @Valid @RequestBody UpdateEventRequest request) {
        EventResponse response = eventService.updateEvent(organizerId, id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<EventResponse> publishEvent(@AuthenticationPrincipal Long organizerId,
                                                      @PathVariable Long id) {
        EventResponse response = eventService.publishEvent(organizerId, id);
        return ResponseEntity.ok(response);
    }
}