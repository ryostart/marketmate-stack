package com.marketmate.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewPostReq(
        @NotBlank String placeId,
        @Min(1) @Max(5) int rating,
        String comment
) {
}
