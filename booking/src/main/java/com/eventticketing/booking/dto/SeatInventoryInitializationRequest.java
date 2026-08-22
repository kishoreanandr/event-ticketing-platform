package com.eventticketing.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SeatInventoryInitializationRequest {
    @NotNull
    private Long eventId;

    @NotNull
    @Min(1)
    private Integer totalSeats;
}
