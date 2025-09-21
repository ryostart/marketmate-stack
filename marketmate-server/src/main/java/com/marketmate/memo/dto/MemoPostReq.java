package com.marketmate.memo.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record MemoPostReq(
        @NotBlank String placeId,
        @NotBlank String title,
        List<String> lines
) {
}
