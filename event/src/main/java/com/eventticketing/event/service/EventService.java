package com.eventticketing.event.service;

import com.eventticketing.event.dto.EventBookingValidationResponse;
import com.eventticketing.event.dto.EventCreateRequest;
import com.eventticketing.event.dto.EventResponse;
import com.eventticketing.event.dto.EventUpdateRequest;

import java.util.List;

public interface EventService {
    EventResponse createEvent(String idempotencyKey, EventCreateRequest eventCreateRequest, Long organizerId);
    List<EventResponse> getAllEvents();
    EventResponse getEventById(Long id);
    EventResponse updateEvent(Long id, EventUpdateRequest request, Long organizerId) throws InterruptedException;
    EventResponse publishEvent(Long id, Long organizerId);
    EventResponse cancelEvent(Long id, Long organizerId);
    EventResponse completeEvent(Long id, Long organizerId);
    EventBookingValidationResponse validateEventForBooking(Long eventId);
}
