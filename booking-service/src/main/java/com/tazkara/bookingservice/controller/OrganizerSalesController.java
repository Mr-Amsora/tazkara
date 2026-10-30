package com.tazkara.bookingservice.controller;

import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.dto.FileDownload;
import com.tazkara.bookingservice.dto.OrganizerBookingResponse;
import com.tazkara.bookingservice.dto.PageResponse;
import com.tazkara.bookingservice.dto.SalesSummaryResponse;
import com.tazkara.bookingservice.security.AuthenticatedUser;
import com.tazkara.bookingservice.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizer/events/{eventId}")
@RequiredArgsConstructor
public class OrganizerSalesController {

    private static final MediaType CSV = MediaType.parseMediaType("text/csv; charset=UTF-8");

    private final SalesService salesService;

    @GetMapping("/bookings")
    public ResponseEntity<PageResponse<OrganizerBookingResponse>> listBookings(
            @AuthenticationPrincipal AuthenticatedUser organizer,
            @PathVariable Long eventId,
            @RequestParam(required = false) BookingStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(salesService.listEventBookings(organizer, eventId, status, pageable));
    }

    @GetMapping("/sales")
    public ResponseEntity<SalesSummaryResponse> getSales(@AuthenticationPrincipal AuthenticatedUser organizer,
                                                         @PathVariable Long eventId) {
        return ResponseEntity.ok(salesService.getSales(organizer, eventId));
    }

    @GetMapping("/bookings/export")
    public ResponseEntity<byte[]> exportBookings(@AuthenticationPrincipal AuthenticatedUser organizer,
                                                 @PathVariable Long eventId) {
        FileDownload file = salesService.exportConfirmedBookings(organizer, eventId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .contentType(CSV)
                .body(file.content());
    }
}