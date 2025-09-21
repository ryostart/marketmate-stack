package com.marketmate.review;

import com.marketmate.common.security.CurrentUserId;
import com.marketmate.review.dto.ReviewDto;
import com.marketmate.review.dto.ReviewPostReq;
import com.marketmate.review.dto.SummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    // 店舗ごとの公開レビュー一覧（認証不要）
    @GetMapping
    public List<ReviewDto> list(@RequestParam String placeId) {
        return service.list(placeId).stream()
                .map(r -> new ReviewDto(
                        r.getId().toString(), r.getUserId().toString(), r.getPlaceId(),
                        r.getRating(), r.getComment(), r.getCreatedAt().toEpochMilli()))
                .collect(Collectors.toList());
    }

    // 投稿（認証必要）
    @PostMapping
    public ReviewDto post(@CurrentUserId UUID userId, @RequestBody ReviewPostReq req) {
        var created = service.add(userId, req.placeId(), req.rating(), req.comment());
        return new ReviewDto(
                created.getId().toString(), created.getUserId().toString(),
                created.getPlaceId(), created.getRating(),
                created.getComment(), created.getCreatedAt().toEpochMilli());
    }

    // まとめ（認証不要）
    @GetMapping("/summary")
    public SummaryDto summary(@RequestParam String placeId) {
        var s = service.summary(placeId);
        return new SummaryDto(s.count(), s.average());
    }
}
