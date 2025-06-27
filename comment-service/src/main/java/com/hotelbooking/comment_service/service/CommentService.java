package com.hotelbooking.comment_service.service;

import com.hotelbooking.comment_service.dto.CommentRequest;
import com.hotelbooking.comment_service.dto.CommentResponse;
import com.hotelbooking.comment_service.model.Comment;
import com.hotelbooking.comment_service.repository.CommentRepository;
import com.hotelbooking.comment_service.util.AuthUtils;
import com.hotelbooking.common_model.BookingQueueDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    @Value("${internal.secret.key}")
    private String internalSecretKey;

    @Value("${booking.service_url}")
    private String bookingServiceUrl;

    private final RestTemplate restTemplate;

    public CommentService(RestTemplate restTemplate, CommentRepository commentRepository) {
        this.restTemplate = restTemplate;
        this.commentRepository = commentRepository;
    }

    public Comment createComment(CommentRequest commentReq) {
        BookingQueueDTO booking = getBooking(commentReq.getBookingId());

        if (booking == null) {
            return null;
        }

        Optional<Comment> commentOptional = commentRepository.findByRoomIdAndUserId(booking.getRoomId(), AuthUtils.getUserId().toString());
        if (commentOptional.isPresent()) {
            return null;
        }

        Comment comment = new Comment();
        comment.setDays(booking.getCheckIn().until(booking.getCheckOut()).getDays());
        comment.setRating(commentReq.getRating());
        comment.setRoomId(booking.getRoomId());
        comment.setHotelId(booking.getHotelId());
        comment.setUserId(AuthUtils.getUserId().toString());

        return commentRepository.save(comment);
    }

    public List<CommentResponse> getCommentsByHotel(Long hotelId) {
        List<Comment> comments = commentRepository.findByHotelId(hotelId);

        return comments.stream().map(this::getCommentResponse).toList();
    }

    public Double getAverageRatingsByHotel(List<CommentResponse> commentResponses) {
        AtomicReference<Double> total = new AtomicReference<>(0.0);
        AtomicInteger count = new AtomicInteger();

        commentResponses.forEach(commentResponse -> {
            commentResponse.getAverageRatings().forEach((service, rating) -> {
                total.updateAndGet(v -> (v + rating));
                count.getAndIncrement();
            });
        });

        return total.get() / count.get();
    }

    private BookingQueueDTO getBooking(Long bookingId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecretKey);
            headers.set("X-User-UserId", AuthUtils.getUserId().toString());
            headers.set("X-User-Role", "USER");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = String.format("%s/api/v1/bookings/%d",
                    bookingServiceUrl, bookingId);

            ResponseEntity<BookingQueueDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    BookingQueueDTO.class
            );

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }

    public CommentResponse getCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setHotelId(comment.getHotelId());
        response.setUserId(comment.getUserId());
        response.setAverageRatings(commentRepository.getAverageRatingsByHotel(comment.getHotelId()));
        response.setAdminAnswer(comment.getAdminAnswer());
        response.setCreatedAt(comment.getCreatedAt());
        response.setDays(comment.getDays());

        return response;
    }
}
