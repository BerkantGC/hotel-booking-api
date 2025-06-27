package com.hotelbooking.hotel_service.repository;

import com.hotelbooking.common_model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    List<Hotel> findAllByLocationContainingIgnoreCase(String location);
}
