package com.eventticketing.booking.exception;

public class BookingHoldExpiredException extends RuntimeException {
    public BookingHoldExpiredException(String message) {
        super(message);
    }
}
