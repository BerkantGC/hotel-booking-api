package com.hotelbooking.hotel_service.repository;

import com.hotelbooking.common_model.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
    boolean existsByAvailableCountGreaterThanAndDateBetween(int count, LocalDate startDate, LocalDate endDate);
    List<RoomAvailability> findByRoomIdAndDateBetween(UUID roomId, LocalDate checkIn, LocalDate checkOut);
    List<RoomAvailability> findByRoomId(UUID roomId);
}
