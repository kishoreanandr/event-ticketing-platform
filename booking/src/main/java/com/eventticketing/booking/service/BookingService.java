    package com.eventticketing.booking.service;

    import com.eventticketing.booking.dto.BookingResponse;
    import com.eventticketing.booking.dto.EventBookingValidationResponse;

    public interface BookingService {
        BookingResponse createBooking(Long userId, EventBookingValidationResponse.BookingCreateRequest request);

        BookingResponse confirmBooking(Long userId,String bookingReference);

        BookingResponse cancelBooking(Long userId,String bookingReference);
    }
