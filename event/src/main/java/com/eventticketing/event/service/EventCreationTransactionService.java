package com.eventticketing.event.service;

import com.eventticketing.event.client.BookingServiceClient;
import com.eventticketing.event.dto.EventCreateRequest;
import com.eventticketing.event.dto.SeatInventoryInitializationRequest;
import com.eventticketing.event.entity.Event;
import com.eventticketing.event.entity.EventIdempotency;
import com.eventticketing.event.entity.EventStatus;
import com.eventticketing.event.entity.IdempotencyStatus;
import com.eventticketing.event.repository.EventIdempotencyRepository;
import com.eventticketing.event.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventCreationTransactionService {
    private final EventRepository eventRepository;
    private final EventIdempotencyRepository eventIdempotencyRepository;
    private final BookingServiceClient bookingServiceClient;

    public EventCreationTransactionService(EventRepository eventRepository,
                                           EventIdempotencyRepository eventIdempotencyRepository,
                                           BookingServiceClient bookingServiceClient){
        this.eventRepository=eventRepository;
        this.eventIdempotencyRepository=eventIdempotencyRepository;
        this.bookingServiceClient=bookingServiceClient;
    }
    @Transactional
    public Event createEventAndMarkCompleted(
            String idempotencyKey,
            EventCreateRequest request,
            Long organizerId) {

        Event event = new Event();

        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setVenue(request.getVenue());
        event.setEventDate(request.getEventDate());
        event.setTotalSeats(request.getTotalSeats());
        event.setAvailableSeats(request.getTotalSeats());
        event.setTicketPrice(request.getTicketPrice());
        event.setStatus(EventStatus.DRAFT);
        event.setOrganizerId(organizerId);

        Event savedEvent = eventRepository.save(event);

        SeatInventoryInitializationRequest seatRequest=
                new SeatInventoryInitializationRequest(savedEvent.getId(),savedEvent.getTotalSeats());

        bookingServiceClient.initializeSeats(seatRequest);

        EventIdempotency record = eventIdempotencyRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() ->
                        new IllegalStateException("Idempotency record not found"));

        record.setEventId(savedEvent.getId());
        record.setStatus(IdempotencyStatus.COMPLETED);
        System.out.println("Saving event: "+savedEvent);
        return savedEvent;
    }
}
