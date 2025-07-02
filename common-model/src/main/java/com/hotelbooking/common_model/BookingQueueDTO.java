package com.hotelbooking.common_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingQueueDTO implements Serializable {
    private Long id;
    private Long hotelId;
    private String hotelName;
    private UUID roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Long userId;
    private int guestCount;
}
