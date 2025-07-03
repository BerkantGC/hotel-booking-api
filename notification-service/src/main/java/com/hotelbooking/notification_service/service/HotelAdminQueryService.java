package com.hotelbooking.notification_service.service;

import com.hotelbooking.common_model.Hotel;
import com.hotelbooking.common_model.HotelCapacityInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelAdminQueryService {
    private final HotelClient hotelClient;

    public HotelAdminQueryService(HotelClient hotelClient) {
        this.hotelClient = hotelClient;
    }

    public List<HotelCapacityInfo> getHotelsWithCapacityBelowThreshold(LocalDate date, int thresholdPercentage) {

        List<HotelCapacityInfo> lowCapacityHotels = new ArrayList<>();

        List<Hotel> hotels = hotelClient.getAllHotels();

        for (Hotel hotel : hotels) {
            int capacity = hotelClient.getCapacityForDate(hotel.getId(), date); // Total rooms
            int available = Integer.parseInt(hotelClient.getAvailableRooms(hotel.getId(), date)); // Rooms available

            int percentAvailable = (int) (((double) available / capacity) * 100);
            if (percentAvailable < thresholdPercentage) {
                lowCapacityHotels.add(new HotelCapacityInfo(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getAdminId(),
                        available
                ));
            }
        }

        return lowCapacityHotels;
    }
}
