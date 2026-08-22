package com.eventticketing.booking.controller;

import com.eventticketing.booking.dto.SeatInventoryInitializationRequest;
import com.eventticketing.booking.dto.SeatInventoryInitializationResponse;
import com.eventticketing.booking.service.SeatInventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/seat-inventory")
public class InternalSeatInventoryController {
    private final SeatInventoryService seatInventoryService;

    public InternalSeatInventoryController(SeatInventoryService seatInventoryService){
        this.seatInventoryService=seatInventoryService;
    }

    @PostMapping("/initialize")
    public ResponseEntity<SeatInventoryInitializationResponse> initializeSeats(
            @Valid @RequestBody SeatInventoryInitializationRequest request){
        SeatInventoryInitializationResponse response= seatInventoryService
                .initializeSeats(request.getEventId(),request.getTotalSeats());
        return ResponseEntity.ok(response);
    }
}
