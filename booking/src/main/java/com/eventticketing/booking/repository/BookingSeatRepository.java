package com.eventticketing.booking.repository;

import com.eventticketing.booking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat,Long> {
    List<BookingSeat> findByBookingId(Long bookingId);

}
