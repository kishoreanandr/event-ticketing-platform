package com.eventticketing.event.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class EventCreateRequest {
    @NotBlank
    @Size(max = 200)
    private String name;
    @Size(max = 1000)
    private String description;
    @NotBlank
    @Size(max = 255)
    private String venue;
    @NotNull
    @Future
    private LocalDateTime eventDate;
    @NotNull
    @Min(1)
    private Integer totalSeats;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal ticketPrice;
}
