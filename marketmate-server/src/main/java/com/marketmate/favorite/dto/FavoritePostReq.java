package com.marketmate.favorite.dto;

import jakarta.validation.constraints.NotBlank;

public record FavoritePostReq(
        @NotBlank String placeId
) {
}
