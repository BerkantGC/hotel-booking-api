package com.hotelbooking.booking_service.repository;

import com.hotelbooking.booking_service.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
