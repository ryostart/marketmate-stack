package com.marketmate.memo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ShoppingMemoServiceTest {

    @InjectMocks
    private ShoppingMemoService service;

    @Mock
    private ShoppingMemoRepository repo;

    @Test
    void list_delegatesToRepository() {
        UUID user = UUID.randomUUID();
        given(repo.findByUserIdOrderByUpdatedAtDesc(user)).willReturn(List.of());

        var result = service.list(user);

        assertThat(result).isEmpty();
        then(repo).should().findByUserIdOrderByUpdatedAtDesc(user);
    }

    @Test
    void add_buildsMemoAndSaves() {
        UUID user = UUID.randomUUID();
        var cap = ArgumentCaptor.forClass(ShoppingMemo.class);
        // save は引数そのまま返すスタブ
        given(repo.save(any(ShoppingMemo.class))).willAnswer(inv -> inv.getArgument(0));

        var saved = service.add(user, "P1", "  Title  ", List.of(" Banana  ", "", "Milk"));

        // save に渡した実体を検査
        then(repo).should().save(cap.capture());
        var toSave = cap.getValue();

        assertThat(toSave.getUserId()).isEqualTo(user);
        assertThat(toSave.getPlaceId()).isEqualTo("P1");
        assertThat(toSave.getTitle()).isEqualTo("Title"); // trim 済み

        assertThat(toSave.getLines()).hasSize(2);
        assertThat(toSave.getLines().get(0).getPos()).isEqualTo(1);
        assertThat(toSave.getLines().get(0).getText()).isEqualTo("Banana");
        assertThat(toSave.getLines().get(1).getPos()).isEqualTo(2);
        assertThat(toSave.getLines().get(1).getText()).isEqualTo("Milk");

        // 呼び出し結果にも同じ内容が載っている（save が echo のため）
        assertThat(saved.getUserId()).isEqualTo(user);
        assertThat(saved.getPlaceId()).isEqualTo("P1");
        assertThat(saved.getTitle()).isEqualTo("Title");
        assertThat(saved.getLines()).hasSize(2);
    }

    @Test
    void remove_callsRepository() {
        UUID user = UUID.randomUUID();
        UUID memoId = UUID.randomUUID();

        service.remove(user, memoId);

        then(repo).should().deleteByIdAndUserId(memoId, user);
    }
}
