package com.hotelbooking.hotel_service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hotelbooking.common_model.Hotel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class HotelResponse implements Serializable {
    private Long id;
    private String name;
    private String location;
    private String description;
    private Double price;
    private Integer roomCount;
    private String image;
    private Double rating;
    private Double latitude;
    private Double longitude;

    public HotelResponse(Hotel hotel, boolean discounted, Double rating) {
        this.name = hotel.getName();
        this.location = hotel.getLocation();
        this.description = hotel.getDescription();
        double basePrice = hotel.getBasePrice().doubleValue();
        this.price = discounted ? basePrice * 0.85 : basePrice;
        this.rating = rating;
        this.roomCount = hotel.getRoomCount();
        this.latitude = hotel.getLatitude();
        this.longitude = hotel.getLongitude();
        this.id = hotel.getId();
        this.image = hotel.getImage();
    }
}
