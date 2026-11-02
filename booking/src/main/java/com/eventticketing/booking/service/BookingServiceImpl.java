package com.eventticketing.booking.service;

import com.eventticketing.booking.client.EventServiceClient;
import com.eventticketing.booking.client.UserServiceClient;
import com.eventticketing.booking.dto.BookingResponse;
import com.eventticketing.booking.dto.EventBookingValidationResponse;
import com.eventticketing.booking.dto.UserBookingValidationResponse;
import com.eventticketing.booking.entity.*;
import com.eventticketing.booking.exception.BookingHoldExpiredException;
import com.eventticketing.booking.exception.InvalidBookingRequestException;
import com.eventticketing.booking.exception.SeatNotAvailableException;
import com.eventticketing.booking.repository.BookingRepository;
import com.eventticketing.booking.repository.BookingSeatRepository;
import com.eventticketing.booking.repository.SeatInventoryRepository;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService{

    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;

    @Value("${booking.hold-duration-minutes}")
    private long holdDurationMinutes;

    public BookingServiceImpl(SeatInventoryRepository seatInventoryRepository,
                              BookingRepository bookingRepository,
                              BookingSeatRepository bookingSeatRepository,
                              UserServiceClient userServiceClient,
                              EventServiceClient eventServiceClient){
        this.seatInventoryRepository=seatInventoryRepository;
        this.bookingRepository=bookingRepository;
        this.bookingSeatRepository=bookingSeatRepository;
        this.userServiceClient=userServiceClient;
        this.eventServiceClient=eventServiceClient;

    }
    

    @Override
    @Transactional
    public BookingResponse createBooking(Long userId, EventBookingValidationResponse.BookingCreateRequest request) {

        validateRequest(request);

        validateUserForBooking(userId);

        validateEventForBooking(request.getEventId());

        List<Integer> requestedSeats=request.getSeatNumbers();

        List<SeatInventory> seats=seatInventoryRepository
                .findAndLockSeats(request.getEventId(),requestedSeats);

        if(seats.size()!=requestedSeats.size()){
            throw new InvalidBookingRequestException(
                    "One or more requested seats do not exist"
            );
        }

        for(SeatInventory seat:seats){
            if(seat.getStatus()!=SeatInventoryStatus.AVAILABLE){
                throw new SeatNotAvailableException(
                        "Seat "+seat.getSeatNumber()+
                                " is not available"
                );
            }
        }

        LocalDateTime holdExpiresAt=
                LocalDateTime.now().plusMinutes(holdDurationMinutes);
        seats.forEach(seat->{
            seat.setStatus(SeatInventoryStatus.HELD);
            seat.setHoldExpiresAt(holdExpiresAt);
        });


        Booking booking=new Booking();

        booking.setBookingReference(generateBookingReference());
        booking.setUserId(userId);
        booking.setEventId(request.getEventId());
        booking.setStatus(BookingStatus.PENDING);

        // Temporary calculation
        booking.setTotalAmount(BigDecimal.ZERO);

        Booking savedBooking=bookingRepository.save(booking);

        for(SeatInventory seat:seats){
            BookingSeat bookingSeat=new BookingSeat();
            bookingSeat.setBooking(savedBooking);
            bookingSeat.setStatus(BookingSeatStatus.HELD);
            bookingSeat.setSeatNumber(seat.getSeatNumber());
            bookingSeat.setEventId(request.getEventId());
            bookingSeatRepository.save(bookingSeat);

            seat.setBookingId(savedBooking.getId());
        }

        return mapToResponse(savedBooking);
    }

    private void validateEventForBooking(@NotNull Long eventId) {
        EventBookingValidationResponse response= eventServiceClient
                .validateEventForBooking(eventId);
        if (!response.isExists()) {
            throw new InvalidBookingRequestException(
                    "Event not found: " + eventId
            );
        }

        if (!response.isBookable()) {
            throw new InvalidBookingRequestException(
                    "Event is not available for booking"
            );
        }
    }

    private void validateUserForBooking(Long userId) {
        UserBookingValidationResponse response= userServiceClient
                .validateUserForBooking(userId);
        if(!response.isExists()){
            throw new InvalidBookingRequestException(
                    "User not found: " + userId
            );
        }
        if (!response.isActive()) {
            throw new InvalidBookingRequestException(
                    "User account is not active"
            );
        }
    }

    private BookingResponse mapToResponse(Booking booking) {

        BookingResponse response = new BookingResponse();

        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setUserId(booking.getUserId());
        response.setEventId(booking.getEventId());
        response.setStatus(booking.getStatus());
        response.setTotalAmount(booking.getTotalAmount());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());

        List<Integer> seatNumbers = bookingSeatRepository
                .findByBookingId(booking.getId())
                .stream()
                .map(BookingSeat::getSeatNumber)
                .toList();

        response.setSeatNumbers(seatNumbers);

        return response;
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(Long userId,String bookingReference) {
        Booking booking=bookingRepository
                .findAndLockByBookingReference(bookingReference)
                .orElseThrow(()->
                        new InvalidBookingRequestException("Booking not found: "+bookingReference));

        if(!booking.getUserId().equals(userId)){
            throw new InvalidBookingRequestException("You are not authorized to confirm this booking");
        }

        if(booking.getStatus()!=BookingStatus.PENDING){
            throw new InvalidBookingRequestException("Only Pending Bookings can be confirmed");
        }

        List<BookingSeat> bookingSeats=
                bookingSeatRepository.findByBookingId(booking.getId());

        if(bookingSeats.isEmpty()){
            throw new InvalidBookingRequestException("Booking doesn't contain any seats!..");
        }

        List<SeatInventory> inventorySeats=seatInventoryRepository
                .findAndLockHeldSeatsByBooking(booking.getEventId(),booking.getId(),SeatInventoryStatus.HELD);

        if(inventorySeats.size()!=bookingSeats.size()){
            throw new InvalidBookingRequestException("One or more seats are no longer held by this booking");
        }

        boolean holdExpired=inventorySeats.stream()
                .anyMatch(s->
                        s.getHoldExpiresAt()==null || !s.getHoldExpiresAt().isAfter(LocalDateTime.now()));
        if(holdExpired){
            throw new BookingHoldExpiredException("The booking hold has been expired");
        }

        inventorySeats.forEach(seat->{
            seat.setStatus(SeatInventoryStatus.BOOKED);
            seat.setHoldExpiresAt(null);
        });

        bookingSeats.forEach(bookingSeat->bookingSeat.setStatus(BookingSeatStatus.CONFIRMED));

        booking.setStatus(BookingStatus.CONFIRMED);

        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long userId, String bookingReference) {

        Booking booking=bookingRepository
                .findAndLockByBookingReference(bookingReference)
                .orElseThrow(() -> new InvalidBookingRequestException("Booking not found: " + bookingReference));

        if(!booking.getUserId().equals(userId)){
            throw new InvalidBookingRequestException("You are not authorized to cancel this booking");
        }

        if(booking.getStatus()==BookingStatus.CANCELLED){
            throw new InvalidBookingRequestException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new InvalidBookingRequestException("Expired booking cannot be cancelled");
        }

        if(booking.getStatus()!=BookingStatus.PENDING &&
        booking.getStatus()!=BookingStatus.CONFIRMED){
            throw new InvalidBookingRequestException("Booking cannot be cancelled in its current state");
        }


        List<SeatInventory> inventorySeats=seatInventoryRepository
                .findAndLockSeatsBooking(booking.getEventId(),booking.getId());
        if(inventorySeats.isEmpty()){
            throw new InvalidBookingRequestException("No seats found for booking: " + bookingReference);
        }

        inventorySeats.forEach(s->{
            s.setBookingId(null);
            s.setHoldExpiresAt(null);
            s.setStatus(SeatInventoryStatus.AVAILABLE);
        });

        List<BookingSeat> bookingSeats= bookingSeatRepository.findByBookingId(booking.getId());

        bookingSeats.forEach(bookingSeat -> bookingSeat.setStatus(BookingSeatStatus.RELEASED));

        booking.setStatus(BookingStatus.CANCELLED);

        return mapToResponse(booking);
    }

    private String generateBookingReference() {
        return "BK-"+
                UUID.randomUUID()
                        .toString()
                        .replace("-","")
                        .substring(0,12)
                        .toUpperCase();
    }

    private void validateRequest(EventBookingValidationResponse.BookingCreateRequest request) {
        if(request==null){
            throw new InvalidBookingRequestException("Invalid Event Id!.. ");
        }
        if(request.getEventId()==null || request.getEventId()<=0){
            throw new InvalidBookingRequestException("Invalid event ID");
        }

        if(request.getSeatNumbers()==null || request.getSeatNumbers().isEmpty()){
            throw new InvalidBookingRequestException("At least one seat must be selected");
        }

        if (request.getSeatNumbers().size() > 10) {

            throw new InvalidBookingRequestException(
                    "Maximum 10 seats can be booked at a time"
            );
        }
        if(new HashSet<>(request.getSeatNumbers()).size()
                !=request.getSeatNumbers().size()){
            throw new InvalidBookingRequestException(
                    "Duplicate seat numbers are not allowed"
            );
        }
    }
}
