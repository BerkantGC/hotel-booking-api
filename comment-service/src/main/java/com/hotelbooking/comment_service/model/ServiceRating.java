package com.hotelbooking.comment_service.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceRating {
    @NotNull
    private ServiceType service;

    @Max(5)
    @Min(1)
    @NotNull
    private int score;
}
