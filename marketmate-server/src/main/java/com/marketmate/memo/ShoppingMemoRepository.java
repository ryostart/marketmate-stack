package com.marketmate.memo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShoppingMemoRepository extends JpaRepository<ShoppingMemo, UUID> {
    @EntityGraph(attributePaths = "lines")
    List<ShoppingMemo> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);
}
