package com.backend.booking.entity;

import com.backend.restaurant.entity.Restaurant;
import com.backend.restaurantTable.entity.RestaurantTable;
import com.backend.timeSlot.entity.TimeSlot;
import com.backend.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = @UniqueConstraint(name = "uk_booking_reference", columnNames = "booking_reference")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "restaurant", "restaurantTable", "timeSlot"})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @NotBlank
    @Column(name = "booking_reference", nullable = false, unique = true, length = 20)
    private String bookingReference;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_user"))
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_restaurant"))
    private Restaurant restaurant;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_table"))
    private RestaurantTable restaurantTable;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_timeslot"))
    private TimeSlot timeSlot;

    @NotNull
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @NotNull
    @Min(1)
    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "special_request", length = 500)
    private String specialRequest;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    /**
     * Optimistic lock — prevents two concurrent requests from double-booking the
     * same table/slot by making the second commit fail with
     * OptimisticLockException instead of silently overwriting the first.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = BookingStatus.PENDING;
        }
    }

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED,
        REJECTED,
        NO_SHOW
    }
}