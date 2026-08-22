package com.eventticketing.event.service;

import com.eventticketing.event.dto.EventCreateRequest;
import com.eventticketing.event.entity.EventIdempotency;
import com.eventticketing.event.entity.IdempotencyStatus;
import com.eventticketing.event.repository.EventIdempotencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class EventIdempotencyServiceImpl implements EventIdempotencyService {

    private final EventIdempotencyRepository eventIdempotencyRepository;
    private final ObjectMapper objectMapper;

    public EventIdempotencyServiceImpl(
            EventIdempotencyRepository eventIdempotencyRepository,
            ObjectMapper objectMapper){
        this.eventIdempotencyRepository=eventIdempotencyRepository;
        this.objectMapper=objectMapper;
    }

    @Override
    public Optional<EventIdempotency> findByIdempotencyKey(String idempotencyKey) {
        return eventIdempotencyRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Override
    public String generateRequestHash(EventCreateRequest request) {
        try{
            String requestJson=objectMapper.writeValueAsString(request);

            MessageDigest messageDigest=MessageDigest.getInstance("SHA-256");

            byte[] hashBytes=messageDigest.digest(requestJson.getBytes(StandardCharsets.UTF_8));

            StringBuilder hash=new StringBuilder();

            for(byte b:hashBytes){
                hash.append(String.format("%02x",b));
            }
            return hash.toString();

        }catch(Exception e){
            throw new IllegalStateException("Unable to generate request hash",e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EventIdempotency createPendingRecord(String idempotencyKey, String requestHash) {
        EventIdempotency record=new EventIdempotency();
        record.setIdempotencyKey(idempotencyKey);
        record.setRequestHash(requestHash);
        record.setStatus(IdempotencyStatus.IN_PROGRESS);
        return eventIdempotencyRepository.saveAndFlush(record);
    }

    @Override
    @Transactional
    public void markCompleted(String idempotencyKey, Long eventId) {
        EventIdempotency record=eventIdempotencyRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseThrow(()-> new IllegalStateException("Idempotency record not found"));
        record.setEventId(eventId);
        record.setStatus(IdempotencyStatus.COMPLETED);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String idempotencyKey) {
        EventIdempotency record=eventIdempotencyRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseThrow(()-> new IllegalStateException("Idempotency record not found"));
        record .setStatus(IdempotencyStatus.FAILED);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean markInProgress(String idempotencyKey) {
        int updatedRows= eventIdempotencyRepository.markInProgressIfFailed(
                idempotencyKey,IdempotencyStatus.FAILED,IdempotencyStatus.IN_PROGRESS);
        return updatedRows==1;
    }
}
