package com.hotelbooking.notification_service.service;

import com.hotelbooking.common_model.Hotel;
import com.hotelbooking.common_model.HotelCapacityInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelAdminQueryService {
    public List<HotelCapacityInfo> getHotelsWithCapacityBelowThreshold(LocalDate date, int thresholdPercentage) {
        // 1. Call hotel-service to fetch all hotels and their capacities
        // 2. For each hotel, check availability for given date
        // 3. If availability < thresholdPercentage, add to result

        List<HotelCapacityInfo> lowCapacityHotels = new ArrayList<>();

        // Example: call hotel-service via REST (use WebClient or RestTemplate)
        List<Hotel> hotels = hotelClient.getAllHotels(); // Assume this exists

        for (Hotel hotel : hotels) {
            int capacity = hotelClient.getCapacityForDate(hotel.getId(), date); // Total rooms
            int available = hotelClient.getAvailableRooms(hotel.getId(), date); // Rooms available

            int percentAvailable = (int) (((double) available / capacity) * 100);
            if (percentAvailable < thresholdPercentage) {
                lowCapacityHotels.add(new HotelCapacityInfo(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getAdminUserId(),
                        available
                ));
            }
        }

        return lowCapacityHotels;
    }

}
