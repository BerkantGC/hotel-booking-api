package com.hotelbooking.common_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomAvailabilityResponse {
    private int count;
    private LocalDate date;
}
