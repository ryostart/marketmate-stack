package com.marketmate.review;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    ReviewRepository repo;

    @InjectMocks
    ReviewService service;

    @Test
    void list_delegatesToRepository() {
        given(repo.findByPlaceIdOrderByCreatedAtDesc("P1")).willReturn(List.of());
        var result = service.list("P1");
        assertThat(result).isEmpty();
        verify(repo).findByPlaceIdOrderByCreatedAtDesc("P1");
    }

    @Test
    void add_savesReviewWithGivenFields() {
        UUID user = UUID.randomUUID();

        // 実際に保存されるエンティティをキャプチャ
        ArgumentCaptor<Review> cap = ArgumentCaptor.forClass(Review.class);
        var saved = Review.builder()
                .id(UUID.randomUUID())
                .userId(user)
                .placeId("P1")
                .rating(4)
                .comment("nice!")
                .createdAt(Instant.parse("2024-02-01T00:00:00Z"))
                .build();
        given(repo.save(any(Review.class))).willReturn(saved);

        var created = service.add(user, "P1", 4, "nice!");

        verify(repo).save(cap.capture());
        var arg = cap.getValue();
        assertThat(arg.getUserId()).isEqualTo(user);
        assertThat(arg.getPlaceId()).isEqualTo("P1");
        assertThat(arg.getRating()).isEqualTo(4);
        assertThat(arg.getComment()).isEqualTo("nice!");

        assertThat(created.getId()).isEqualTo(saved.getId());
    }

    @Test
    void summary_combinesCountAndAverage() {
        given(repo.countByPlaceId("SHOP")).willReturn(3L);
        given(repo.avg("SHOP")).willReturn(4.5d);

        var s = service.summary("SHOP");

        assertThat(s.count()).isEqualTo(3L);
        assertThat(s.average()).isEqualTo(4.5d);
        verify(repo).countByPlaceId("SHOP");
        verify(repo).avg("SHOP");
    }
}
