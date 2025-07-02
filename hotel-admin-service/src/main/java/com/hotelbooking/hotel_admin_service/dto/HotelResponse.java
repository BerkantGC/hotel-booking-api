package com.hotelbooking.hotel_admin_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelResponse {
    private Long id;
    private String name;
    private String image;
    private String location;
    private String description;
    private Double latitude;
    private Double longitude;
    private BigDecimal basePrice;
    @JsonProperty("room_count")
    private Integer room_count;
}
