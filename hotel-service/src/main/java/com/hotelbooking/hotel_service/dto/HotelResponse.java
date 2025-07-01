package com.hotelbooking.hotel_service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.hotelbooking.common_model.Hotel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class HotelResponse implements Serializable {
    private Long id;
    private String name;
    private String location;
    private String description;
    private Double starRating;
    private Double price;
    private Integer roomCount;
    private String image;

    public HotelResponse(Hotel hotel, boolean discounted) {
        this.name = hotel.getName();
        this.location = hotel.getLocation();
        this.description = hotel.getDescription();
        this.starRating = hotel.getStarRating();
        double basePrice = hotel.getBasePrice().doubleValue();
        this.price = discounted ? basePrice * 0.85 : basePrice;
        this.roomCount = hotel.getRoomCount();
        this.id = hotel.getId();
        this.image = hotel.getImage();
    }
}
