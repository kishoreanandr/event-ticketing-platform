package com.eventticketing.event.service;

import com.eventticketing.event.dto.EventCreateRequest;
import com.eventticketing.event.entity.EventIdempotency;

import java.util.Optional;

public interface EventIdempotencyService {
    Optional<EventIdempotency> findByIdempotencyKey(String idempotencyKey);
    String generateRequestHash(EventCreateRequest request);
    EventIdempotency createPendingRecord(String idempotencyKey, String requestHash);
    void markCompleted(String idempotencyKey,Long eventId);
    void markFailed(String idempotencyKey);
    boolean markInProgress(String idempotencyKey);
}
