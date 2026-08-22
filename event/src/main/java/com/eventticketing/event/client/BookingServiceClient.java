package com.eventticketing.event.client;

import com.eventticketing.event.dto.SeatInventoryInitializationRequest;
import com.eventticketing.event.dto.SeatInventoryInitializationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "booking-service",url = "${services.booking.url}")
public interface BookingServiceClient {

    @PostMapping("/internal/seat-inventory/initialize")
    SeatInventoryInitializationResponse initializeSeats(@RequestBody SeatInventoryInitializationRequest request);
}
