package com.hotelbooking.hotel_service.repository;

import com.hotelbooking.common_model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

}

