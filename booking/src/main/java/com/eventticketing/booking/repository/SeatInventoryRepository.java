    package com.eventticketing.booking.repository;

    import com.eventticketing.booking.entity.BookingSeat;
    import com.eventticketing.booking.entity.SeatInventory;
    import com.eventticketing.booking.entity.SeatInventoryStatus;
    import jakarta.persistence.LockModeType;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Lock;
    import org.springframework.data.jpa.repository.Query;
    import org.springframework.data.repository.query.Param;

    import java.util.List;

    public interface SeatInventoryRepository extends JpaRepository<SeatInventory,Long> {

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM SeatInventory s " +
                "WHERE s.eventId = :eventId " +
                "AND s.seatNumber IN :seatNumbers " +
                "ORDER BY s.seatNumber")
        List<SeatInventory> findAndLockSeats(@Param("eventId") Long eventId,
                                             @Param("seatNumbers") List<Integer> seatNumbers);


        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM SeatInventory s " +
                "WHERE s.eventId= :eventId " +
                "AND s.bookingId= :bookingId " +
                "AND s.status= :seatInventoryStatus")
        List<SeatInventory> findAndLockHeldSeatsByBooking(@Param("eventId") Long eventId,
                                                        @Param("bookingId") Long bookingId,
                                                        @Param("seatInventoryStatus") SeatInventoryStatus seatInventoryStatus);


        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT s FROM SeatInventory s " +
                "WHERE s.eventId = :eventId " +
                "AND s.bookingId = :bookingId")
        List<SeatInventory> findAndLockSeatsBooking(@Param("eventId") Long eventId,
                                                    @Param("bookingId") Long bookingId);

        boolean existsByEventIdAndSeatNumber(Long eventId, int seatNumber);
    }
