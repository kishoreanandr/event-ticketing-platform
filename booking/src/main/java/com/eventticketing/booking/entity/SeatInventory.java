package com.eventticketing.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Table(name="seat_inventory",uniqueConstraints = {
        @UniqueConstraint(name = "uk_event_seat",columnNames = {"event_id","seat_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private SeatInventoryStatus status;

    private Long bookingId;

    private LocalDateTime holdExpiresAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate(){

    }
}
