package com.example.seonchaksoon.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Event {

    @Id
    @Column(name = "event_key", length = 100)
    private String eventKey;

    @Column(name = "limit_count", nullable = false)
    private int limitCount;

    @Column(name = "issued_count", nullable = false)
    private int issuedCount;

    @Version
    private Long version;

    public Event(String eventKey, int limitCount) {
        this.eventKey = eventKey;
        this.limitCount = limitCount;
        this.issuedCount = 0;
    }

    public boolean isSoldOut() {
        return issuedCount >= limitCount;
    }

    public void increaseIssued() {
        this.issuedCount += 1;
    }
}
