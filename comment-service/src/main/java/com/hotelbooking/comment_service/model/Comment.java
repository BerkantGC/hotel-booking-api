package com.hotelbooking.comment_service.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Document(collection = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    private String id;

    @Indexed
    @Field("hotel_id")
    private Long hotelId;

    @Field("room_id")
    private UUID roomId;

    @Field("user_id")
    private String userId;

    @Field("admin_answer")
    private String adminAnswer;

    private List<ServiceRating> rating;

    private Integer days;

    @Field("created_at")
    @CreatedDate
    private LocalDate createdAt;

    @Field("admin_answered_at")
    private LocalDate adminAnsweredAt;
}
