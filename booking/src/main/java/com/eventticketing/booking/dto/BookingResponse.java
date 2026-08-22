package com.eventticketing.booking.dto;

import com.eventticketing.booking.entity.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookingResponse {

    private Long id;

    private String bookingReference;

    private Long userId;

    private Long eventId;

    private BookingStatus status;

    private BigDecimal totalAmount;

    private List<Integer> seatNumbers;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}