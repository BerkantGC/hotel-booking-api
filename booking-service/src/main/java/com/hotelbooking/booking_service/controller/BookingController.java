package com.hotelbooking.booking_service.controller;

import com.hotelbooking.booking_service.dto.BookingRequest;
import com.hotelbooking.booking_service.model.Booking;
import com.hotelbooking.booking_service.service.BookingService;
import com.hotelbooking.booking_service.util.AuthUtils;
import com.hotelbooking.common_model.BookingQueueDTO;
import com.hotelbooking.common_model.PagedResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/my_bookings")
    public ResponseEntity<Iterable<BookingQueueDTO>> getBookings() {
        Long userId = AuthUtils.getUserId();

        return ResponseEntity.ok(bookingService.getBookings(userId));
    }

    @GetMapping("/my_bookings/paged")
    public ResponseEntity<PagedResponse<BookingQueueDTO>> getBookingsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Long userId = AuthUtils.getUserId();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<BookingQueueDTO> response = bookingService.getBookingsPaged(userId, pageable);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public BookingQueueDTO showBooking(@PathVariable Long id) {
        Long userId = AuthUtils.getUserId();

        return bookingService.showBooking(id, userId);
    }
}