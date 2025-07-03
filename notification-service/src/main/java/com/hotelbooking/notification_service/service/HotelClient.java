package com.hotelbooking.notification_service.service;

import com.hotelbooking.common_model.Hotel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
public class HotelClient {
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "http://hotel-service/api/hotels";

    public HotelClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Hotel> getAllHotels() {
        ResponseEntity<List<Hotel>> response = restTemplate.exchange(
            BASE_URL,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Hotel>>() {}
        );
        return response.getBody();
    }

    public int getCapacityForDate(Long hotelId, LocalDate date) {
        ResponseEntity<Integer> response = restTemplate.exchange(
            BASE_URL + "/{hotelId}/capacity?date={date}",
            HttpMethod.GET,
            null,
            Integer.class,
            hotelId,
            date
        );
        return response.getBody();
    }

    public String getAvailableRooms(Long hotelId, LocalDate date) {
        ResponseEntity<String> response = restTemplate.exchange(
            BASE_URL + "/{hotelId}/available-rooms?date={date}",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<String>() {},
            hotelId,
            date
        );
        return response.getBody();
    }
}