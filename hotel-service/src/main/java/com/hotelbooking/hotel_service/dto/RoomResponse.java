package com.hotelbooking.hotel_service.dto;

import com.hotelbooking.common_model.Room;
import com.hotelbooking.common_model.RoomAvailabilityResponse;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RoomResponse {
    UUID id;
    Long hotelId;
    Room.RoomKind roomKind;
    Integer guestCount;
    boolean available;
    List<RoomAvailabilityResponse> availablityList;

    public RoomResponse(Room room, boolean available) {
        this.id = room.getId();
        this.hotelId = room.getHotel().getId();
        this.roomKind = room.getKind();
        this.guestCount = room.getGuestCount();
        this.available = available;
    }
}
