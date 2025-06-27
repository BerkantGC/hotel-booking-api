package com.hotelbooking.comment_service.dto;

import com.hotelbooking.comment_service.model.ServiceRating;
import lombok.Data;

import java.util.List;

@Data
public class CommentRequest {
    private Long bookingId;
    private List<ServiceRating> rating;
}
