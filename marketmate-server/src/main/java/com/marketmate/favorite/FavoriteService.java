package com.marketmate.favorite;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository repo;

    @Transactional(readOnly = true)
    public List<Favorite> list(UUID userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Favorite add(UUID userId, String placeId) {
        return repo.findFirstByUserIdAndPlaceId(userId, placeId)
                .orElseGet(() -> repo.save(Favorite.builder()
                        .userId(userId).placeId(placeId).build()));
    }

    @Transactional
    public void remove(UUID userId, String placeId) {
        repo.deleteByUserIdAndPlaceId(userId, placeId);
    }
}
