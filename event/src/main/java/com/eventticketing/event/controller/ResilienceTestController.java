package com.eventticketing.event.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResilienceTestController {

    @GetMapping("/internal/test/slow")
    public String slowResponse(@RequestParam(defaultValue = "8000") long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
        System.out.println("Delayed: "+delayMs);
        return  "Event Service responded after "
                + delayMs + " milliseconds";
    }
}
