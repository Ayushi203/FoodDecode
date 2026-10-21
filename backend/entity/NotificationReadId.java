package com.fooddecode.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 40)
    private String kind;

    @Column(name = "ref_id")
    private Long refId;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NotificationReadId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
                && Objects.equals(kind, that.kind)
                && Objects.equals(refId, that.refId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, kind, refId);
    }
}
