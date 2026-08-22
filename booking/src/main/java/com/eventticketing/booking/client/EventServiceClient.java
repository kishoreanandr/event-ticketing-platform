package com.eventticketing.booking.client;

import com.eventticketing.booking.dto.EventBookingValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service",
             url ="${services.event.url}" )
public interface EventServiceClient {

    @GetMapping("/internal/events/{eventId}/booking-validation")
    EventBookingValidationResponse validateEventForBooking(@PathVariable("eventId") Long eventId);
}
