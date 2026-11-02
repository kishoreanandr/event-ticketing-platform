package com.eventticketing.booking.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResilienceTestController{
    private final EventServiceClient eventServiceClient;

    public ResilienceTestController(EventServiceClient eventServiceClient){
        this.eventServiceClient=eventServiceClient;
    }

    @GetMapping("/internal/test/event-timeout")
    public String testEventTimeout(@RequestParam(defaultValue = "8000") long delayMs){
        return eventServiceClient.testSlowResponse(delayMs);
    }

}
