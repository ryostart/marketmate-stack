package com.marketmate.favorite;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    FavoriteRepository repo;

    @InjectMocks
    FavoriteService service;

    @Test
    void list_delegatesToRepository() {
        UUID user = UUID.randomUUID();
        given(repo.findByUserIdOrderByCreatedAtDesc(user)).willReturn(List.of());
        assertThat(service.list(user)).isEmpty();
        then(repo).should().findByUserIdOrderByCreatedAtDesc(user);
    }

    @Test
    void add_savesWhenNotExists() {
        UUID user = UUID.randomUUID();
        String place = "P";
        given(repo.findFirstByUserIdAndPlaceId(user, place)).willReturn(Optional.empty());

        ArgumentCaptor<Favorite> cap = ArgumentCaptor.forClass(Favorite.class);
        given(repo.save(cap.capture())).willAnswer(inv ->
                Favorite.builder()
                        .userId(cap.getValue().getUserId())
                        .placeId(cap.getValue().getPlaceId())
                        .build());

        Favorite created = service.add(user, place);

        assertThat(created.getUserId()).isEqualTo(user);
        assertThat(created.getPlaceId()).isEqualTo(place);
        then(repo).should().save(any(Favorite.class));
    }

    @Test
    void add_isNoOpWhenAlreadyExists() {
        UUID user = UUID.randomUUID();
        String place = "P";
        Favorite existing = Favorite.builder().userId(user).placeId(place).build();
        given(repo.findFirstByUserIdAndPlaceId(user, place)).willReturn(Optional.of(existing));

        Favorite got = service.add(user, place);

        assertThat(got).isSameAs(existing);
        then(repo).should(never()).save(any());
    }

    @Test
    void remove_callsDelete() {
        UUID user = UUID.randomUUID();
        String place = "Q";

        service.remove(user, place);

        then(repo).should().deleteByUserIdAndPlaceId(user, place);
    }
}
