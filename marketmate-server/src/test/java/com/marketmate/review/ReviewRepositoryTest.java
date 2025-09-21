package com.marketmate.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // ← PostgreSQLをそのまま使う
class ReviewRepositoryTest {

    @Autowired
    ReviewRepository repo;

    @Test
    void save_find_summary_works() {
        // 保存
        var r1 = repo.save(Review.builder()
                .userId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .placeId("SHOP")
                .rating(5)
                .comment("great")
                .build());

        var r2 = repo.save(Review.builder()
                .userId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .placeId("SHOP")
                .rating(3)
                .comment("ok")
                .build());

        // 取得（降順）
        List<Review> list = repo.findByPlaceIdOrderByCreatedAtDesc("SHOP");
        assertThat(list).hasSize(2);

        // サマリ
        long count = repo.countByPlaceId("SHOP");
        double avg = repo.avg("SHOP");

        assertThat(count).isEqualTo(2);
        assertThat(avg).isBetween(3.99, 5.01)  // 5 と 3 の平均=4.0 をだいたいで検証
                .isEqualTo(4.0);
    }
}
