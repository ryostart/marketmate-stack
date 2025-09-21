package com.marketmate.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    List<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Favorite> findFirstByUserIdAndPlaceId(UUID userId, String placeId);

    void deleteByUserIdAndPlaceId(UUID userId, String placeId);
}
