package com.tazkara.eventservice.controller;

import com.tazkara.eventservice.dto.EventResponse;
import com.tazkara.eventservice.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventResponse>> browseEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(eventService.browseEvents(city, from, to, keyword, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventDetails(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getPublicEventDetails(id));
    }
}