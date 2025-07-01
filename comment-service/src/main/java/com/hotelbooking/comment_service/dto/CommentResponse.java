package com.hotelbooking.comment_service.dto;

import com.hotelbooking.comment_service.model.ServiceRating;
import com.hotelbooking.comment_service.model.ServiceType;
import com.hotelbooking.common_model.User;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class CommentResponse {
    private String id;
    private Long hotelId;
    private User user;
    private String text;
    private String adminAnswer;
    private Integer days;
    private LocalDate createdAt;
    private String adminAnsweredAt;
    private List<ServiceRating> ratings;
}
