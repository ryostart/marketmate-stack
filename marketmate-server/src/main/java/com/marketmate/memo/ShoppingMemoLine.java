package com.marketmate.memo;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "shopping_memo_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingMemoLine {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memo_id", nullable = false)
    private ShoppingMemo memo;

    @Column(nullable = false)
    private int pos;
    @Column(nullable = false, length = 200)
    private String text;

    @PrePersist
    void pre() {
        if (id == null) id = UUID.randomUUID();
    }
}
