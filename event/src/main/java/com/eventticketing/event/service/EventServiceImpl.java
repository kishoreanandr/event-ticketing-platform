package com.eventticketing.event.service;

import com.eventticketing.event.dto.EventBookingValidationResponse;
import com.eventticketing.event.dto.EventCreateRequest;
import com.eventticketing.event.dto.EventResponse;
import com.eventticketing.event.dto.EventUpdateRequest;
import com.eventticketing.event.entity.Event;
import com.eventticketing.event.entity.EventIdempotency;
import com.eventticketing.event.entity.EventStatus;
import com.eventticketing.event.entity.IdempotencyStatus;
import com.eventticketing.event.exception.EventNotFoundException;
import com.eventticketing.event.exception.InvalidEventStatusException;
import com.eventticketing.event.repository.EventIdempotencyRepository;
import com.eventticketing.event.repository.EventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventIdempotencyService eventIdempotencyService;
    private final EventIdempotencyRepository eventIdempotencyRepository;
    private final EventCreationTransactionService eventCreationTransactionService;

    public EventServiceImpl(EventRepository eventRepository,
                            EventIdempotencyService eventIdempotencyService,
                            EventIdempotencyRepository eventIdempotencyRepository,
                            EventCreationTransactionService eventCreationTransactionService) {
        this.eventRepository = eventRepository;
        this.eventIdempotencyService=eventIdempotencyService;
        this.eventIdempotencyRepository=eventIdempotencyRepository;
        this.eventCreationTransactionService=eventCreationTransactionService;
    }

    @Override
    public EventResponse createEvent(String idempotencyKey, EventCreateRequest request, Long organizerId) {

        String requestHash=eventIdempotencyService.generateRequestHash(request);

        Optional<EventIdempotency> existingRecord=eventIdempotencyRepository.findByIdempotencyKey(idempotencyKey);
        boolean retryRequest = false;
        if(existingRecord.isPresent()){
            EventIdempotency record=existingRecord.get();

            if(!record.getRequestHash().equals(requestHash)){
                throw new IllegalArgumentException(
                    "Idempotency key was already used with a different request"
                );
            }
            if(record.getStatus()== IdempotencyStatus.COMPLETED
                    && record.getEventId()!=null){
                Event exsistingEvent=eventRepository
                        .findById(record.getEventId())
                        .orElseThrow(
                                ()->new EventNotFoundException("Event associated with idempotency record was not found"));
                if (!exsistingEvent.getOrganizerId().equals(organizerId)) {
                    throw new AccessDeniedException(
                            "You are not allowed to access this event"
                    );
                }
                return convertToResponse(exsistingEvent);
            }
            if(record.getStatus()==IdempotencyStatus.IN_PROGRESS){
                throw new IllegalStateException("Request with this idempotency key is already being processed");
            }
            if(record.getStatus()==IdempotencyStatus.FAILED){
                boolean retryAcquired=eventIdempotencyService.markInProgress(idempotencyKey);
                if(!retryAcquired){
                    throw new IllegalStateException(
                            "Request with this idempotency key is already being retried"
                    );
                }
                retryRequest = true;
            }



        }

        if(!retryRequest && existingRecord.isEmpty()) {
            try {
                eventIdempotencyService
                        .createPendingRecord(idempotencyKey, requestHash);

            } catch (DataIntegrityViolationException exception) {

                Optional<EventIdempotency> currentRecord =
                        eventIdempotencyService.findByIdempotencyKey(idempotencyKey);
                if (currentRecord.isEmpty()) {
                    throw new IllegalStateException("Unable to resolve idempotency record after concurrent request", exception);
                }
                EventIdempotency record = currentRecord.get();
                if (!record.getRequestHash().equals(requestHash)) {
                    throw new IllegalStateException("Idempotency key was already used with a different request");
                }
                throw new IllegalStateException(
                        "Request with this idempotency key is already being processed"
                );

            }
        }

        try {
            Event event=eventCreationTransactionService.
                    createEventAndMarkCompleted(idempotencyKey,request,organizerId);
            return convertToResponse(event);
        }catch(Exception exception){
            eventIdempotencyService.markFailed(idempotencyKey);
            throw exception;
        }


    }


    @Override
    public List<EventResponse> getAllEvents() {
        List<Event> events=eventRepository.findAll();
        return events
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public EventResponse getEventById(Long id) {
        Event event=eventRepository
                .findById(id)
                .orElseThrow(()-> new EventNotFoundException("Event not found with id: "+id));
        return convertToResponse(event);
    }

    @Transactional
    @Override
    public EventResponse updateEvent(Long id, EventUpdateRequest request, Long organizerId) throws InterruptedException {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException(
                                "Event not found with id: " + id
                        ));

        validateOrganizerOwnership(event,organizerId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new InvalidEventStatusException(
                    "Only DRAFT events can be updated"
            );
        }

        event.setName(request.getName());
        event.setEventDate(request.getEventDate());
        event.setTicketPrice(request.getTicketPrice());
        event.setVenue(request.getVenue());
        event.setDescription(request.getDescription());

        Event updatedEvent = eventRepository.saveAndFlush(event);

        return convertToResponse(updatedEvent);
    }

    @Override
    @Transactional
    public EventResponse publishEvent(Long id, Long organizerId) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new EventNotFoundException("Event not found with id: "+ id));
        validateOrganizerOwnership(event,organizerId);
        if(event.getStatus()!=EventStatus.DRAFT){
            throw new InvalidEventStatusException("Only DRAFT events can be published");
        }
        event.setStatus(EventStatus.PUBLISHED);
        Event updatedEvent=eventRepository.saveAndFlush(event);
        return convertToResponse(updatedEvent);
    }

    @Override
    @Transactional
    public EventResponse cancelEvent(Long id, Long organizerId) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new EventNotFoundException("Event not found with id: "+ id));

        validateOrganizerOwnership(event,organizerId);

        if(event.getStatus()==EventStatus.COMPLETED || event.getStatus()==EventStatus.CANCELLED){
            throw new InvalidEventStatusException("Only DRAFT or PUBLISHED events can be cancelled");
        }
        event.setStatus(EventStatus.CANCELLED);
        Event updatedEvent=eventRepository.saveAndFlush(event);
        return convertToResponse(updatedEvent);
    }

    @Override
    @Transactional
    public EventResponse completeEvent(Long id, Long organizerId) {
        Event event=eventRepository.findById(id)
                .orElseThrow(()->new EventNotFoundException("Event not found with id: "+ id));

        validateOrganizerOwnership(event,organizerId);

        if(event.getStatus()!=EventStatus.PUBLISHED){
            throw new InvalidEventStatusException("Only PUBLISHED events can be completed");
        }
//        if (event.getEventDate().isAfter(LocalDateTime.now())) {
//            throw new InvalidEventStatusException(
//                    "Event cannot be completed before the event date"
//            );
//        }
        event.setStatus(EventStatus.COMPLETED);
        Event updatedEvent=eventRepository.saveAndFlush(event);
        return convertToResponse(updatedEvent);
    }

    @Override
    public EventBookingValidationResponse validateEventForBooking(Long eventId) {
        return eventRepository.findById(eventId)
                .map(event->new EventBookingValidationResponse(
                        event.getId(),
                        true,
                        event.getStatus()==EventStatus.PUBLISHED,
                        event.getStatus()
                ))
                .orElseGet(()->new EventBookingValidationResponse(
                        eventId,
                        false,
                        false,
                        null
                ));
    }

    private EventResponse convertToResponse(Event event) {

        EventResponse response = new EventResponse();

        response.setId(event.getId());
        response.setName(event.getName());
        response.setDescription(event.getDescription());
        response.setVenue(event.getVenue());
        response.setEventDate(event.getEventDate());
        response.setTotalSeats(event.getTotalSeats());
        response.setAvailableSeats(event.getAvailableSeats());
        response.setTicketPrice(event.getTicketPrice());
        response.setStatus(event.getStatus());
        response.setOrganizerId(event.getOrganizerId());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        response.setVersion(event.getVersion());

        return response;
    }

    private void validateOrganizerOwnership(
            Event event,
            Long organizerId) {

        if (!event.getOrganizerId().equals(organizerId)) {
            throw new AccessDeniedException(
                    "You are not allowed to modify this event"
            );
        }
    }
}