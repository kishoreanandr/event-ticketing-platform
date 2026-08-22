package com.eventticketing.event.controller;

import com.eventticketing.event.dto.EventBookingValidationResponse;
import com.eventticketing.event.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/events")
public class InternalEventController {
    private final EventService eventService;

    public InternalEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{eventId}/booking-validation")
    public ResponseEntity<EventBookingValidationResponse> validateEventForBooking(
            @PathVariable Long eventId) {

        EventBookingValidationResponse response =
                eventService.validateEventForBooking(eventId);

        return ResponseEntity.ok(response);
    }
}
