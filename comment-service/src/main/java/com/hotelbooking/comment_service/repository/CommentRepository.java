package com.hotelbooking.comment_service.repository;

import com.hotelbooking.comment_service.model.Comment;
import com.hotelbooking.comment_service.repository.custom.CommentAggregationRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends MongoRepository<Comment, Long>, CommentAggregationRepository {
    List<Comment> findByRoomId(UUID roomId);
    Optional<Comment> findByRoomIdAndUserId(UUID roomId, String userId);
    List<Comment> findByHotelId(Long hotelId);
}
