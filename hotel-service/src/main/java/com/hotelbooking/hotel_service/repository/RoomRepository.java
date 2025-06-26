package com.hotelbooking.hotel_service.repository;

import com.hotelbooking.common_model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    @Query("SELECT r FROM Room r WHERE " +
            "LOWER(r.hotel.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "AND r.capacity >= :roomCount " +
            "AND r.startDate <= :checkInDate AND r.endDate >= :checkOutDate " +
            "AND r.availableCount > 0")
    List<Room> searchRooms(
            @Param("location") String location,
            @Param("roomCount") int roomCount,
            @Param("checkInDate") Date checkIn,
            @Param("checkOutDate") Date checkOut);
}

