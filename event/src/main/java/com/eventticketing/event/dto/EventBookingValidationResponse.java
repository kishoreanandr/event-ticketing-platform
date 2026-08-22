package com.eventticketing.event.dto;

import com.eventticketing.event.entity.EventStatus;
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
