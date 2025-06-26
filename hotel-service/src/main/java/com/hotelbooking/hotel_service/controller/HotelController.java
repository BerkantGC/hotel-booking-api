package com.hotelbooking.hotel_service.controller;

import com.hotelbooking.hotel_service.dto.HotelResponse;
import com.hotelbooking.hotel_service.service.HotelService;
import com.hotelbooking.hotel_service.util.AuthUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hotels")
public class HotelController {

    private final HotelService hotelService;

    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getHotels() {
        boolean discounted = AuthUtils.isSignedIn();
        return ResponseEntity.ok(hotelService.findAll(discounted));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        boolean discounted = AuthUtils.isSignedIn();
        return ResponseEntity.ok(hotelService.findById(id, discounted));
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotels(
            @RequestParam String location,
            @RequestParam int roomCount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {

        boolean discounted = AuthUtils.isSignedIn();
        List<HotelResponse> response = hotelService.search(location, roomCount, checkIn, checkOut, discounted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hotelId}/rooms/{roomId}/availability")
    public ResponseEntity<Boolean> checkRoomAvailability(
            @PathVariable Long hotelId,
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam int guestCount) {

        boolean available = hotelService.isRoomAvailable(hotelId, roomId, checkIn, checkOut, guestCount);
        return ResponseEntity.ok(available);
    }

    // Cache management endpoints (for admin use)
    @PostMapping("/cache/evict/all")
    public ResponseEntity<String> evictAllCache() {
        hotelService.evictAllCache();
        return ResponseEntity.ok("All hotel caches evicted successfully");
    }

    @PostMapping("/cache/evict/hotels")
    public ResponseEntity<String> evictHotelsCache() {
        hotelService.evictAllCache();
        return ResponseEntity.ok("Hotels cache evicted successfully");
    }

    @PostMapping("/cache/evict/hotel/{id}")
    public ResponseEntity<String> evictHotelCache(@PathVariable Long id) {
        hotelService.evictHotelCache(id);
        return ResponseEntity.ok("Hotel cache for ID " + id + " evicted successfully");
    }
}