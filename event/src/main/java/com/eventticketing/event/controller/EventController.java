package com.eventticketing.event.controller;

import java.util.List;

import com.eventticketing.common_security.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eventticketing.event.dto.EventCreateRequest;
import com.eventticketing.event.dto.EventResponse;
import com.eventticketing.event.dto.EventUpdateRequest;
import com.eventticketing.event.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService=eventService;
    }

    @PostMapping("/")
    public ResponseEntity<EventResponse> createEvent(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody EventCreateRequest eventCreateRequest) {
        Long organizerId= SecurityUtils.getCurrentUserId();
        EventResponse eventCreationResponse =eventService.createEvent(idempotencyKey,eventCreateRequest,organizerId);
        return new ResponseEntity<>(eventCreationResponse, HttpStatus.CREATED);
    }

    @GetMapping("/")
    public ResponseEntity<List<EventResponse>> getAllEvents(){
        List<EventResponse> allEventResponse=eventService.getAllEvents();
        return new ResponseEntity<>(allEventResponse,HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id){
        EventResponse idEventResponse= eventService.getEventById(id);
        return new ResponseEntity<>(idEventResponse,HttpStatus.OK);
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequest updateRequest) throws InterruptedException {
        Long organizerId= SecurityUtils.getCurrentUserId();

        EventResponse updateResponse= eventService.updateEvent(eventId,updateRequest,organizerId);
        return new ResponseEntity<>(updateResponse,HttpStatus.OK);
    }

    @PatchMapping("/{eventId}/publish")
    public ResponseEntity<EventResponse> publishEvent(@PathVariable Long eventId) {
        Long organizerId= SecurityUtils.getCurrentUserId();

        EventResponse response = eventService.publishEvent(eventId,organizerId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{eventId}/cancel")
    public ResponseEntity<EventResponse> cancelEvent(@PathVariable Long eventId) {
        Long organizerId= SecurityUtils.getCurrentUserId();

        EventResponse response = eventService.cancelEvent(eventId,organizerId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{eventId}/complete")
    public ResponseEntity<EventResponse> completeEvent(@PathVariable Long eventId) {
        Long organizerId= SecurityUtils.getCurrentUserId();

        EventResponse response = eventService.completeEvent(eventId,organizerId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
