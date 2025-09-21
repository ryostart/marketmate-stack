package com.marketmate.memo;

import com.marketmate.common.Ids;
import com.marketmate.common.security.CurrentUserId;
import com.marketmate.memo.dto.MemoDto;
import com.marketmate.memo.dto.MemoPostReq;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/memos")
@RequiredArgsConstructor
public class ShoppingMemoController {

    private final ShoppingMemoService service;

    // 買い物メモ一覧取得
    @GetMapping
    public List<MemoDto> list(@CurrentUserId UUID userId) {
        return service.list(userId).stream()
                .map(m -> new MemoDto(
                        m.getId().toString(), m.getUserId().toString(),
                        m.getPlaceId(), m.getTitle(),
                        m.getLines().stream().map(ShoppingMemoLine::getText).toList(),
                        m.getCreatedAt().toEpochMilli(), m.getUpdatedAt().toEpochMilli()))
                .collect(Collectors.toList());
    }

    // 買い物メモ作成
    @PostMapping
    public MemoDto create(@CurrentUserId UUID userId, @RequestBody MemoPostReq req) {
        var created = service.add(userId, req.placeId(), req.title(), req.lines());
        return new MemoDto(
                created.getId().toString(), created.getUserId().toString(),
                created.getPlaceId(), created.getTitle(),
                created.getLines().stream().map(ShoppingMemoLine::getText).toList(),
                created.getCreatedAt().toEpochMilli(), created.getUpdatedAt().toEpochMilli());
    }

    // 買い物メモ削除
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id, @CurrentUserId UUID userId) {
        service.remove(userId, Ids.uuid(id));
    }
}
