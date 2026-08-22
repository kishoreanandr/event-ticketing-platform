package com.eventticketing.event.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventUpdateRequest {
    @NotBlank
    @Size(max=200)
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
    @DecimalMin(value="0.0",inclusive = true)
    private BigDecimal ticketPrice;
}
