package com.marketmate.review.dto;

public record ReviewDto(
        String id,
        String userId,
        String placeId,
        int rating,
        String comment,
        long createdAt
) {
}
