package com.eventticketing.event.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "idempotency_records",uniqueConstraints = {
            @UniqueConstraint(name="uk_idempotent_key",columnNames = "idempotency_key")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class EventIdempotency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String idempotencyKey;

    @Column(nullable = false,length = 64)
    private String requestHash;

    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private IdempotencyStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt=LocalDateTime.now();
    }
}
