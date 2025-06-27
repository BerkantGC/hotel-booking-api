package com.hotelbooking.comment_service.repository.custom;

import com.hotelbooking.comment_service.model.Comment;
import com.hotelbooking.comment_service.model.ServiceType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class CommentAggregationRepositoryImpl implements CommentAggregationRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public CommentAggregationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Map<ServiceType, Double> getAverageRatingsByHotel(Long hotelId) {
        MatchOperation matchHotel = match(Criteria.where("hotel_id").is(hotelId));
        UnwindOperation unwindRatings = unwind("rating");
        GroupOperation groupByService = group("rating.service")
                .first("rating.service").as("service")
                .avg("rating.score").as("average");


        Aggregation aggregation = newAggregation(matchHotel, unwindRatings, groupByService);

        AggregationResults<DocumentResult> results = mongoTemplate.aggregate(
                aggregation, Comment.class, DocumentResult.class
        );

        Map<ServiceType, Double> output = new HashMap<>();
        for (DocumentResult result : results) {
            output.put(result.getService(), result.getAverage());
        }
        return output;
    }

    @Getter
    @Setter
    private static class DocumentResult {
        private ServiceType service;
        private Double average;
    }
}
