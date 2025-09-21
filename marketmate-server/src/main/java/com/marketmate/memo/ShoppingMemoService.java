package com.marketmate.memo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingMemoService {
    private final ShoppingMemoRepository repo;

    @Transactional(readOnly = true)
    public List<ShoppingMemo> list(UUID userId) {
        return repo.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional
    public ShoppingMemo add(UUID userId, String placeId, String title, List<String> lines) {
        var memo = ShoppingMemo.builder()
                .userId(userId).placeId(placeId).title(title == null ? "" : title.trim())
                .build();
        int pos = 1;
        for (String raw : (lines == null ? List.<String>of() : lines)) {
            var t = raw == null ? "" : raw.trim();
            if (t.isEmpty()) continue;
            memo.getLines().add(ShoppingMemoLine.builder().memo(memo).pos(pos++).text(t).build());
        }
        return repo.save(memo);
    }

    @Transactional
    public void remove(UUID userId, UUID memoId) {
        repo.deleteByIdAndUserId(memoId, userId);
    }
}
