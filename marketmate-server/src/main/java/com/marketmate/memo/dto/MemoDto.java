package com.marketmate.memo.dto;

import java.util.List;

public record MemoDto(
        String id,
        String userId,
        String placeId,
        String title,
        List<String> lines,
        long createdAt,
        long updatedAt
) {
}
