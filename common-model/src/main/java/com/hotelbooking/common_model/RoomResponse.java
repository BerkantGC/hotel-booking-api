package com.hotelbooking.common_model;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RoomResponse {
    private UUID id;
    private Long hotel_id;
    private Room.RoomKind kind;
    private List<RoomAvailabilityResponse> availablityList;
}

