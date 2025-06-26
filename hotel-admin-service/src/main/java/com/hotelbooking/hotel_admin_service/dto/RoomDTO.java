package com.hotelbooking.hotel_admin_service.dto;

import com.hotelbooking.common_model.Room.RoomKind;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RoomDTO {
    @NotNull
    private Long hotelId;

    @NotNull
    private int capacity;

    @NotNull
    private RoomKind kind;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private int availableCount;
}
