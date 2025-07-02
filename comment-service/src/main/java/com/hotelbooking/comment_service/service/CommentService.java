package com.hotelbooking.comment_service.service;

import com.hotelbooking.comment_service.dto.CommentRequest;
import com.hotelbooking.comment_service.dto.CommentResponse;
import com.hotelbooking.comment_service.model.Comment;
import com.hotelbooking.comment_service.model.ServiceType;
import com.hotelbooking.comment_service.repository.CommentRepository;
import com.hotelbooking.comment_service.util.AuthUtils;
import com.hotelbooking.common_model.BookingQueueDTO;
import com.hotelbooking.common_model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class CommentService {
    private final CommentRepository commentRepository;
    @Value("${internal.secret.key}")
    private String internalSecretKey;

    @Value("${booking.service_url}")
    private String bookingServiceUrl;

    @Value("${user.service_url}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

    public CommentService(RestTemplate restTemplate, CommentRepository commentRepository) {
        this.restTemplate = restTemplate;
        this.commentRepository = commentRepository;
    }

    public Comment createComment(CommentRequest commentReq) {
        BookingQueueDTO booking = getBooking(commentReq.getBookingId());
        log.info("Booking: {}", booking);
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

    public CommentResponse getCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setHotelId(comment.getHotelId());
        response.setUser(getUser(Long.parseLong(comment.getUserId())));
        response.setRatings(comment.getRating());
        response.setAdminAnswer(comment.getAdminAnswer());
        response.setCreatedAt(comment.getCreatedAt());
        response.setDays(comment.getDays());
        response.setText(comment.getText());
        return response;
    }

    public List<CommentResponse> getCommentsByHotel(Long hotelId) {
        List<Comment> comments = commentRepository.findByHotelId(hotelId);

        return comments.stream().map(this::getCommentResponse).toList();
    }

    public Double getAverageRatingsByHotel(List<CommentResponse> commentResponses) {
        AtomicReference<Double> total = new AtomicReference<>(0.0);
        AtomicInteger count = new AtomicInteger();

        commentResponses.forEach(commentResponse -> commentResponse.getRatings().forEach((serviceRating) -> {
            total.updateAndGet(v -> (v + serviceRating.getScore()));
            count.getAndIncrement();
        }));

        return total.get() / count.get();
    }

    public Map<ServiceType, Double> getAllServiceAverages(List<CommentResponse> commentResponses) {
        // Map from ServiceType to List of scores
        Map<ServiceType, List<Double>> serviceScores = new java.util.EnumMap<>(ServiceType.class);

        commentResponses.forEach(comment ->
                comment.getRatings().forEach(rating -> serviceScores.computeIfAbsent(rating.getService(), k -> new java.util.ArrayList<>())
                        .add((double) rating.getScore()))
        );

        // Compute average for each service
        return serviceScores.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0)
                ));
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

    private User getUser(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Internal-Secret", internalSecretKey);
            headers.set("X-User-UserId", userId.toString());
            headers.set("X-User-Role", "USER");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = String.format("%s/api/v1/users/%d",
                    userServiceUrl, userId);

            ResponseEntity<User> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    User.class
            );

            if (response.getStatusCode().is4xxClientError()) {
                return null;
            }

            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
}