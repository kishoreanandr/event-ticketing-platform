package com.eventticketing.booking.entity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Value;

import java.util.List;

@Data
public class BookingCreateRequest {
    @NotNull
    private Long eventId;

    @NotEmpty
    @Size(min = 1,max=10)
    private List< @NotNull @Positive Integer> seatNumbers;
}
