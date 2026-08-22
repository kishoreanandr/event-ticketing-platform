package com.eventticketing.booking.controller;

import com.eventticketing.booking.dto.BookingResponse;
import com.eventticketing.booking.entity.BookingCreateRequest;
import com.eventticketing.booking.service.BookingService;
import com.eventticketing.common_security.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService){
        this.bookingService=bookingService;
    }
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingCreateRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();
        BookingResponse response =
                bookingService.createBooking(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{bookingReference}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable String bookingReference){
        Long userId=SecurityUtils.getCurrentUserId();
        BookingResponse response=bookingService.confirmBooking(userId,bookingReference);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bookingReference}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String bookingReference) {

        Long userId = SecurityUtils.getCurrentUserId();


        BookingResponse booking = bookingService.cancelBooking(userId, bookingReference);

        return ResponseEntity.ok(booking);
    }
}
