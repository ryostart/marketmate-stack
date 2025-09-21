package com.marketmate.favorite.dto;

public record FavoriteDto(
        String placeId,
        String userId,
        long createdAt
) {
}
