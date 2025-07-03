package com.hotelbooking.hotel_admin_service.controller;

import com.hotelbooking.hotel_admin_service.dto.HotelDTO;
import com.hotelbooking.common_model.Hotel;
import com.hotelbooking.common_model.PagedResponse;
import com.hotelbooking.hotel_admin_service.dto.HotelResponse;
import com.hotelbooking.hotel_admin_service.service.HotelService;
import com.hotelbooking.hotel_admin_service.util.AuthUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/hotels")
@PreAuthorize("hasRole('ADMIN')")
public class HotelController {

    private final HotelService service;

    public HotelController(HotelService service) {
        this.service = service;
    }

    @GetMapping
    public List<HotelResponse> getAll() {
        return service.getAllHotels(AuthUtils.getUserId());
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<HotelResponse>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<HotelResponse> response = service.getAllHotelsPaged(AuthUtils.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Hotel> create(@ModelAttribute HotelDTO hotelDTO) {
        try {
            Hotel hotel = service.addHotel(hotelDTO, AuthUtils.getUserId());
            return ResponseEntity.ok(hotel);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public Hotel update(@PathVariable Long id, @ModelAttribute HotelDTO hotelDTO) {
        return service.updateHotel(id, hotelDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteHotel(id);
        return ResponseEntity.ok().build();
    }
}

