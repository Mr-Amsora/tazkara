package com.tazkara.bookingservice.controller;

import com.tazkara.bookingservice.domain.BookingStatus;
import com.tazkara.bookingservice.dto.BookingResponse;
import com.tazkara.bookingservice.dto.BookingSummaryResponse;
import com.tazkara.bookingservice.dto.FileDownload;
import com.tazkara.bookingservice.dto.PageResponse;
import com.tazkara.bookingservice.dto.PayBookingRequest;
import com.tazkara.bookingservice.dto.PlaceBookingRequest;
import com.tazkara.bookingservice.security.AuthenticatedUser;
import com.tazkara.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendee/bookings")
@RequiredArgsConstructor
public class AttendeeBookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> placeBooking(@AuthenticationPrincipal AuthenticatedUser user,
                                                        @Valid @RequestBody PlaceBookingRequest request) {
        BookingResponse response = bookingService.placeBooking(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookingSummaryResponse>> listMyBookings(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) BookingStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.listMyBookings(user, status, pageable));
    }

    @GetMapping("/{reference}")
    public ResponseEntity<BookingResponse> getBooking(@AuthenticationPrincipal AuthenticatedUser user,
                                                      @PathVariable String reference) {
        return ResponseEntity.ok(bookingService.getBooking(user, reference));
    }

    @PostMapping("/{reference}/pay")
    public ResponseEntity<BookingResponse> payBooking(@AuthenticationPrincipal AuthenticatedUser user,
                                                      @PathVariable String reference,
                                                      @Valid @RequestBody PayBookingRequest request) {
        return ResponseEntity.ok(bookingService.payBooking(user, reference, request));
    }

    @PostMapping("/{reference}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @PathVariable String reference) {
        return ResponseEntity.ok(bookingService.cancelBooking(user, reference));
    }

    @GetMapping("/{reference}/tickets")
    public ResponseEntity<byte[]> downloadTickets(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @PathVariable String reference) {
        FileDownload file = bookingService.downloadTickets(user, reference);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.fileName()).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(file.content());
    }
}