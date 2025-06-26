package com.hotelbooking.booking_service.controller;

import com.hotelbooking.booking_service.dto.BookingRequest;
import com.hotelbooking.booking_service.model.Booking;
import com.hotelbooking.booking_service.service.BookingService;
import com.hotelbooking.booking_service.util.AuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/bookings")
@PreAuthorize("isAuthenticated()")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {
        try {
            Long userId = AuthUtils.getUserId();

            Booking booking = bookingService.createBooking(request, userId);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            log.error("Error creating booking: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}