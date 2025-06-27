package com.hotelbooking.hotel_admin_service.controller;

import com.hotelbooking.common_model.RoomResponse;
import com.hotelbooking.hotel_admin_service.dto.RoomDTO;
import com.hotelbooking.hotel_admin_service.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        try {
            roomService.createRoom(roomDTO);
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        try {
            return ResponseEntity.ok(roomService.getRooms());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).build();
        }
    }
}
