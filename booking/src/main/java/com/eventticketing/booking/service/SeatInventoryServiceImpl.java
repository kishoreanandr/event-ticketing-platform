package com.eventticketing.booking.service;

import com.eventticketing.booking.dto.SeatInventoryInitializationResponse;
import com.eventticketing.booking.entity.SeatInventory;
import com.eventticketing.booking.entity.SeatInventoryStatus;
import com.eventticketing.booking.repository.SeatInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatInventoryServiceImpl implements SeatInventoryService{

    private final SeatInventoryRepository seatInventoryRepository;

    public SeatInventoryServiceImpl(SeatInventoryRepository seatInventoryRepository){
        this.seatInventoryRepository=seatInventoryRepository;
    }

    @Override
    @Transactional
    public SeatInventoryInitializationResponse initializeSeats(Long eventId, Integer totalSeats) {
        int createdSeats=0;

        for(int seatNumber=1;seatNumber<=totalSeats;seatNumber++){
            if (seatInventoryRepository.existsByEventIdAndSeatNumber(eventId,seatNumber)){
                continue;
            }
            SeatInventory seat=new SeatInventory();
            seat.setEventId(eventId);
            seat.setSeatNumber(seatNumber);
            seat.setStatus(SeatInventoryStatus.AVAILABLE);
            seat.setBookingId(null);
            seat.setHoldExpiresAt(null);

            seatInventoryRepository.save(seat);
            createdSeats++;
        }
        return new SeatInventoryInitializationResponse(eventId,
                totalSeats,createdSeats,
                createdSeats==totalSeats? "Seat inventory initialized successfully"
                        :"Seat inventory already partially initialized");
    }
}
