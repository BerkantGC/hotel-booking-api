package com.hotelbooking.comment_service.repository.custom;

import com.hotelbooking.comment_service.model.ServiceType;

import java.util.Map;

public interface CommentAggregationRepository {
    Map<ServiceType, Double> getAverageRatingsByHotel(Long hotelId);
}