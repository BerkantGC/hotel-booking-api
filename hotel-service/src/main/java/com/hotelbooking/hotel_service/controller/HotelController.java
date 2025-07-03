package com.hotelbooking.hotel_service.controller;

import com.hotelbooking.common_model.BookingQueueDTO;
import com.hotelbooking.common_model.PagedResponse;
import com.hotelbooking.hotel_service.dto.HotelResponse;
import com.hotelbooking.hotel_service.dto.RoomResponse;
import com.hotelbooking.hotel_service.service.HotelService;
import com.hotelbooking.hotel_service.util.AuthUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<HotelResponse>> getHotelsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        boolean discounted = AuthUtils.isSignedIn();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(hotelService.findAllPaged(discounted, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        return ResponseEntity.ok(hotelService.findById(id));
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

    @GetMapping("/search/paged")
    public ResponseEntity<PagedResponse<HotelResponse>> searchHotelsPaged(
            @RequestParam String location,
            @RequestParam int roomCount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        boolean discounted = AuthUtils.isSignedIn();

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<HotelResponse> response = hotelService.searchPaged(location, roomCount, checkIn, checkOut, discounted, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<List<RoomResponse>> getHotelRooms(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int guestCount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        System.out.println("Fetching rooms for hotel ID: " + id + " with dates: " + checkIn + " - " + checkOut);
        List<RoomResponse> response = hotelService.getHotelRooms(id, checkIn, checkOut, guestCount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/rooms/paged")
    public ResponseEntity<PagedResponse<RoomResponse>> getHotelRoomsPaged(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int guestCount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        System.out.println("Fetching rooms for hotel ID: " + id + " with dates: " + checkIn + " - " + checkOut);

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<RoomResponse> response = hotelService.getHotelRoomsPaged(id, checkIn, checkOut, guestCount, pageable);

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

    @PostMapping("/{hotelId}/update_availability")
    public ResponseEntity<String> updateAvailabilityAfterBooking(
            @PathVariable Long hotelId,
            @RequestBody BookingQueueDTO request
    ) {
        hotelService.updateAvailability(hotelId, request);
        return ResponseEntity.ok("Availability updated successfully");
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