package com.hotelbooking.booking_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingRequest {
    @JsonProperty("hotel_id")
    private Long hotelId;
    @JsonProperty("room_id")
    private UUID roomId;
    @JsonProperty("check_in")
    private LocalDate checkIn;
    @JsonProperty("check_out")
    private LocalDate checkOut;
    @JsonProperty("guest_count")
    private int guestCount;
}
