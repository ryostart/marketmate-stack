package com.marketmate.favorite;

import com.marketmate.common.security.CurrentUserId;
import com.marketmate.favorite.dto.FavoriteDto;
import com.marketmate.favorite.dto.FavoritePostReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Validated
public class FavoriteController {

    private final FavoriteService service;

    // お気に入り取得
    @GetMapping
    public List<FavoriteDto> list(@CurrentUserId UUID userId) {
        return service.list(userId).stream()
                .map(f -> new FavoriteDto(
                        f.getPlaceId(),
                        f.getUserId().toString(),
                        f.getCreatedAt().toEpochMilli()))
                .toList();
    }

    // お気に入り追加
    @PostMapping
    public FavoriteDto add(@CurrentUserId UUID userId, @RequestBody @Valid FavoritePostReq req) {
        var created = service.add(userId, req.placeId());
        return new FavoriteDto(
                created.getPlaceId(),
                created.getUserId().toString(),
                created.getCreatedAt().toEpochMilli());
    }

    // お気に入り削除
    @DeleteMapping("/{placeId}")
    public void remove(@CurrentUserId UUID userId, @PathVariable String placeId) {
        service.remove(userId, placeId);
    }
}
