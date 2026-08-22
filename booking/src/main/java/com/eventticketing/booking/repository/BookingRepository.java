package com.eventticketing.booking.repository;

import com.eventticketing.booking.entity.Booking;
import com.eventticketing.booking.entity.BookingStatus;
import com.eventticketing.booking.entity.SeatInventoryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b " +
            "WHERE b.bookingReference = :bookingReference")
    Optional<Booking> findAndLockByBookingReference(
            @Param("bookingReference") String bookingReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b " +
            "JOIN SeatInventory s ON s.bookingId=b.id " +
            "WHERE b.status = :bookingStatus " +
            "AND s.status = :seatInventoryStatus " +
            "AND s.holdExpiresAt <= :currentTime")
    List<Booking> findAndLockExpiredBookings(
            @Param("currentTime") LocalDateTime currentTime,
            @Param("bookingStatus") BookingStatus bookingStatus,
            @Param("seatInventoryStatus")SeatInventoryStatus seatInventoryStatus);
}
