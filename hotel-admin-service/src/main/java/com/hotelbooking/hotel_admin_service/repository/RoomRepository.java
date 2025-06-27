package com.hotelbooking.hotel_admin_service.repository;

import com.hotelbooking.common_model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotelId(Long hotelId);

    Optional<Room> findById(UUID id);

    Optional<Room> findByHotelIdAndKind(Long hotelId, Room.RoomKind kind);
}

