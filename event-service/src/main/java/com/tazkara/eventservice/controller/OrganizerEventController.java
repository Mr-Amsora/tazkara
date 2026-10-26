package com.tazkara.eventservice.controller;

import com.tazkara.eventservice.dto.CreateEventRequest;
import com.tazkara.eventservice.dto.EventResponse;
import com.tazkara.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}