package com.hotelbooking.comment_service.dto;

import com.hotelbooking.comment_service.model.ServiceType;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
public class CommentResponse {
    private String id;
    private Long hotelId;
    private String userId;
    private String adminAnswer;
    private Integer days;
    private LocalDate createdAt;
    private String adminAnsweredAt;
    private Map<ServiceType, Double> averageRatings;
}
