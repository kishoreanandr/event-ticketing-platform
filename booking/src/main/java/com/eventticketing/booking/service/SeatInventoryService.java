package com.eventticketing.booking.service;

import com.eventticketing.booking.dto.SeatInventoryInitializationResponse;

public interface SeatInventoryService {
    SeatInventoryInitializationResponse initializeSeats(Long eventId,Integer totalSeats);
}
