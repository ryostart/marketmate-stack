package com.marketmate.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository repo;

    @Transactional(readOnly = true)
    public List<Review> list(String placeId) {
        return repo.findByPlaceIdOrderByCreatedAtDesc(placeId);
    }

    @Transactional
    public Review add(UUID userId, String placeId, int rating, String comment) {
        return repo.save(Review.builder()
                .userId(userId).placeId(placeId).rating(rating).comment(comment).build());
    }

    @Transactional(readOnly = true)
    public Summary summary(String placeId) {
        return new Summary(repo.countByPlaceId(placeId), repo.avg(placeId));
    }

    public record Summary(long count, double average) {
    }
}
