package com.eventticketing.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EventBookingValidationResponse {
    private Long eventId;
    private boolean exists;
    private boolean bookable;
    private EventStatus status;

    @Data
    public static class BookingCreateRequest {
        @NotNull
        private Long eventId;

        @NotEmpty
        @Size(min = 1,max=10)
        private List< @NotNull @Positive Integer> seatNumbers;
    }
}
