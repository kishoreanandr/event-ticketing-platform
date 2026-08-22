package com.eventticketing.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventBookingValidationResponse {
    private Long eventId;
    private boolean exists;
    private boolean bookable;
    private EventStatus status;
}
