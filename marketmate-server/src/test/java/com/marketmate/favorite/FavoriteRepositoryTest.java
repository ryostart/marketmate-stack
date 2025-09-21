package com.marketmate.favorite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest(properties = {
        // ★ テスト時は Hibernate にテーブルを作らせる（Flyway には依存しない）
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // 置き換え禁止
class FavoriteRepositoryTest {

    // ★ Postgres をテスト起動（バージョンはお好みで）
    @Container
    @ServiceConnection   // Spring Boot が DataSource を自動配線
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    FavoriteRepository repo;

    @Test
    void save_find_delete_works() {
        UUID user = UUID.randomUUID();

        repo.save(Favorite.builder().userId(user).placeId("A").build());
        repo.save(Favorite.builder().userId(user).placeId("B").build());

        List<Favorite> list = repo.findByUserIdOrderByCreatedAtDesc(user);
        assertThat(list).hasSize(2);

        assertThat(repo.findFirstByUserIdAndPlaceId(user, "A")).isPresent();

        repo.deleteByUserIdAndPlaceId(user, "A");
        assertThat(repo.findFirstByUserIdAndPlaceId(user, "A")).isEmpty();
    }
}
