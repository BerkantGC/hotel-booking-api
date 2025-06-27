package com.hotelbooking.hotel_admin_service.repository;

import com.hotelbooking.common_model.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
    Optional<RoomAvailability> findByRoomIdAndDate(UUID roomId, LocalDate date);
    List<RoomAvailability> findByRoomId(UUID roomId);
}
