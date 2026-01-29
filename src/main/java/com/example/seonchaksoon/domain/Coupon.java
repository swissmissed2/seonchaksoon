package com.example.seonchaksoon.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupon",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_coupon_event_user",
                columnNames = {"event_key", "user_id"}
        )
)
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="event_key", nullable = false, length = 100)
    private String eventKey;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @PrePersist
    public void prePersist() {
        this.issuedAt = LocalDateTime.now();
    }

}
