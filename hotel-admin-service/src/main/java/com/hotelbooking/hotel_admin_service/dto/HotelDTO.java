package com.hotelbooking.hotel_admin_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
public class HotelDTO {
    private String name;
    private MultipartFile image;
    private String location;
    private String description;
    private Double latitude;
    private Double longitude;
    private BigDecimal basePrice;
    @JsonProperty("room_count")
    private Integer room_count;
}
