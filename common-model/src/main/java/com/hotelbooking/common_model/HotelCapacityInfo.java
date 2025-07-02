package com.hotelbooking.common_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotelCapacityInfo {
    private Long hotelId;
    private String hotelName;
    private Long adminUserId;
    private int remainingCapacity;
}
