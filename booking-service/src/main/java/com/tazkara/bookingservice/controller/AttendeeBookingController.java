package com.tazkara.bookingservice.controller;

import com.tazkara.bookingservice.dto.BookingResponse;
import com.tazkara.bookingservice.dto.PayBookingRequest;
import com.tazkara.bookingservice.dto.PlaceBookingRequest;
import com.tazkara.bookingservice.security.AuthenticatedUser;
import com.tazkara.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/{reference}/pay")
    public ResponseEntity<BookingResponse> payBooking(@AuthenticationPrincipal AuthenticatedUser user,
                                                      @PathVariable String reference,
                                                      @Valid @RequestBody PayBookingRequest request) {
        return ResponseEntity.ok(bookingService.payBooking(user, reference, request));
    }
}