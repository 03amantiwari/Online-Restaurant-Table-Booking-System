package com.backend.booking.repository;

import com.backend.booking.entity.Booking;
import com.backend.restaurant.entity.Restaurant;
import com.backend.restaurantTable.entity.RestaurantTable;
import com.backend.timeSlot.entity.TimeSlot;
import com.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ---- Customer: "my bookings" ----
    List<Booking> findByUserOrderByBookingDateDescCreatedAtDesc(User user);

    // ---- Owner/Admin: restaurant-wide views ----
    List<Booking> findByRestaurant(Restaurant restaurant);

    List<Booking> findByRestaurantAndBookingDate(Restaurant restaurant, LocalDate bookingDate);

    List<Booking> findByRestaurantAndTimeSlot(Restaurant restaurant, TimeSlot timeSlot);

    // ---- Double-booking prevention ----
    // true if this table already has an active (PENDING/CONFIRMED) booking for that date+slot
    boolean existsByRestaurantTableAndBookingDateAndTimeSlotAndStatusIn(
            RestaurantTable restaurantTable,
            LocalDate bookingDate,
            TimeSlot timeSlot,
            List<Booking.BookingStatus> statuses
    );

    // ---- Reference generation uniqueness check ----
    boolean existsByBookingReference(String bookingReference);
}
