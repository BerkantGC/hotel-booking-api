package com.hotelbooking.hotel_admin_service.controller;

import com.hotelbooking.common_model.Room;
import com.hotelbooking.hotel_admin_service.dto.RoomDTO;
import com.hotelbooking.hotel_admin_service.repository.HotelRepository;
import com.hotelbooking.hotel_admin_service.repository.RoomRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@RestController
@RequestMapping("/api/v1/admin/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomController(RoomRepository roomRepository, HotelRepository hotelRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        return hotelRepository.findById(roomDTO.getHotelId())
                .map(hotel -> {
                    Room room = new Room();
                    room.setHotel(hotel);
                    room.setCapacity(roomDTO.getCapacity());
                    room.setKind(roomDTO.getKind());
                    room.setStartDate(Date.valueOf(roomDTO.getStartDate()));
                    room.setEndDate(Date.valueOf(roomDTO.getEndDate()));
                    room.setAvailableCount(roomDTO.getAvailableCount());
                    Room saved = roomRepository.save(room);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.badRequest().build());
    }
}
