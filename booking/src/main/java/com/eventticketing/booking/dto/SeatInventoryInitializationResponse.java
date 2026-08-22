package com.eventticketing.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatInventoryInitializationResponse {
    private Long eventId;
    private Integer totalSeats;
    private Integer createdSeats;
    private String message;
}
