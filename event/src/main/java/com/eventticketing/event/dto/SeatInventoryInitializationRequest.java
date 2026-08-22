package com.eventticketing.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatInventoryInitializationRequest {
    private Long eventId;
    private Integer totalSeats;
}
