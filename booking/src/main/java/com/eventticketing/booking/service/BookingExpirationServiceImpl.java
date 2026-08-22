package com.eventticketing.booking.service;

import com.eventticketing.booking.client.EventServiceClient;
import com.eventticketing.booking.client.UserServiceClient;
import com.eventticketing.booking.entity.*;
import com.eventticketing.booking.repository.BookingRepository;
import com.eventticketing.booking.repository.BookingSeatRepository;
import com.eventticketing.booking.repository.SeatInventoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingExpirationServiceImpl implements BookingExpirationService{

    private final BookingRepository bookingRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;


    public BookingExpirationServiceImpl(BookingRepository bookingRepository,
                                        SeatInventoryRepository seatInventoryRepository,
                                        BookingSeatRepository bookingSeatRepository,
                                        UserServiceClient userServiceClient,
                                        EventServiceClient eventServiceClient){
        this.bookingRepository=bookingRepository;
        this.seatInventoryRepository=seatInventoryRepository;
        this.bookingSeatRepository=bookingSeatRepository;
        this.userServiceClient=userServiceClient;
        this.eventServiceClient=eventServiceClient;
    }

    @Override
    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void expireBookings() {

        LocalDateTime now=LocalDateTime.now();

        List<Booking> expiredBookings=bookingRepository
                .findAndLockExpiredBookings(now, BookingStatus.PENDING, SeatInventoryStatus.HELD);
        if(expiredBookings==null)
            return;

        for(Booking booking:expiredBookings){
            expireBooking(booking,now);
        }

    }

    private void expireBooking(Booking booking, LocalDateTime now) {

        if(booking.getStatus()!=BookingStatus.PENDING)
            return;

        List<SeatInventory> seats=seatInventoryRepository
                .findAndLockHeldSeatsByBooking(booking.getEventId(),booking.getId(),SeatInventoryStatus.HELD);

        if(seats.isEmpty())
            return;
        boolean expired=seats.stream()
                .anyMatch(seat->
                        seat.getHoldExpiresAt()!=null &&
                        !seat.getHoldExpiresAt().isAfter(now));
        if(!expired)
            return;

        List<BookingSeat> bookingSeats=bookingSeatRepository.findByBookingId(booking.getId());

        seats.forEach(seat->{
            seat.setBookingId(null);
            seat.setHoldExpiresAt(null);
            seat.setStatus(SeatInventoryStatus.AVAILABLE);
        });

        bookingSeats.stream()
                .filter(bookingSeat->bookingSeat.getStatus()==BookingSeatStatus.HELD)
                .forEach(bookingSeat->bookingSeat.setStatus(BookingSeatStatus.RELEASED));

        booking.setStatus(BookingStatus.EXPIRED);
    }
}
