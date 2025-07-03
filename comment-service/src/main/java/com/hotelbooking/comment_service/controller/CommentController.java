package com.hotelbooking.comment_service.controller;

import com.hotelbooking.comment_service.dto.CommentRequest;
import com.hotelbooking.comment_service.dto.CommentResponse;
import com.hotelbooking.comment_service.model.Comment;
import com.hotelbooking.comment_service.service.CommentService;
import com.hotelbooking.comment_service.util.AuthUtils;
import com.hotelbooking.common_model.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        if(!AuthUtils.isSignedIn())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Comment created = commentService.createComment(request);

        if (created == null) {
            return ResponseEntity.unprocessableEntity().build();
        }
        return ResponseEntity.ok(commentService.getCommentResponse(created));
    }


    @GetMapping
    public ResponseEntity<Map<String, Object>> getCommentsByHotel(@RequestParam Long hotelId) {
        List<CommentResponse> comments = commentService.getCommentsByHotel(hotelId);
        Map<String, Object> response = new java.util.HashMap<>();

        response.put("comments", comments);
        response.put("overall_rating", commentService.getAverageRatingsByHotel(comments));
        response.put("service_ratings", commentService.getAllServiceAverages(comments));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getCommentsByHotelPaged(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<CommentResponse> pagedComments = commentService.getCommentsByHotelPaged(hotelId, pageable);
        
        // Get all comments for ratings calculation
        List<CommentResponse> allComments = commentService.getCommentsByHotel(hotelId);
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("comments", pagedComments);
        response.put("overall_rating", commentService.getAverageRatingsByHotel(allComments));
        response.put("service_ratings", commentService.getAllServiceAverages(allComments));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get_rating")
    public ResponseEntity<Double> getHotelRating(@RequestParam Long hotelId) {
        List<CommentResponse> comments = commentService.getCommentsByHotel(hotelId);
        return ResponseEntity.ok(commentService.getAverageRatingsByHotel(comments));
    }
}
