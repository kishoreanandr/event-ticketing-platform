package com.eventticketing.event.repository;

import com.eventticketing.event.entity.EventIdempotency;
import com.eventticketing.event.entity.IdempotencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventIdempotencyRepository extends JpaRepository<EventIdempotency,Long> {
    Optional<EventIdempotency> findByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query("UPDATE EventIdempotency e " +
            "SET e.status= :targetStatus, e.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE e.idempotencyKey= :idempotencyKey " +
            "AND e.status= :currentStatus")
    int markInProgressIfFailed(@Param("idempotencyKey") String idempotencyKey,
                               @Param("currentStatus") IdempotencyStatus currentStatus,
                                @Param("targetStatus") IdempotencyStatus targetStatus);
}
