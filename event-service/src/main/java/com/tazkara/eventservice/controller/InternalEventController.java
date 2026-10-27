package com.tazkara.eventservice.controller;

import com.tazkara.eventservice.dto.EventInfoResponse;
import com.tazkara.eventservice.dto.ReserveRequest;
import com.tazkara.eventservice.dto.ReservedLineResponse;
import com.tazkara.eventservice.service.EventService;
import com.tazkara.eventservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final EventService eventService;
    private final InventoryService inventoryService;

    @GetMapping("/{eventId}")
    public ResponseEntity<EventInfoResponse> getEventInfo(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventInfo(eventId));
    }

    @PostMapping("/{eventId}/reserve")
    public ResponseEntity<List<ReservedLineResponse>> reserve(@PathVariable Long eventId,
                                                              @Valid @RequestBody ReserveRequest request) {
        return ResponseEntity.ok(inventoryService.reserve(eventId, request));
    }

    @PostMapping("/{eventId}/release")
    public ResponseEntity<Void> release(@PathVariable Long eventId,
                                        @Valid @RequestBody ReserveRequest request) {
        inventoryService.release(eventId, request);
        return ResponseEntity.noContent().build();
    }
}