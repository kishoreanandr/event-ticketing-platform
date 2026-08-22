    package com.eventticketing.booking.service;

    import com.eventticketing.booking.dto.BookingResponse;
    import com.eventticketing.booking.entity.Booking;
    import com.eventticketing.booking.entity.BookingCreateRequest;

    public interface BookingService {
        BookingResponse createBooking(Long userId, BookingCreateRequest request);

        BookingResponse confirmBooking(Long userId,String bookingReference);

        BookingResponse cancelBooking(Long userId,String bookingReference);
    }
