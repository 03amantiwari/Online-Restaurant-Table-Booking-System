package com.backend.timeSlot.repository;

import com.backend.restaurant.entity.Restaurant;
import com.backend.timeSlot.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    // Not used by the Booking module yet, but a natural fit here for when the
    // TimeSlot module gets its own "list slots for a restaurant" endpoint.
    List<TimeSlot> findByRestaurant(Restaurant restaurant);

	Optional<TimeSlot> findByIdAndRestaurantId(Long id, Long restId);
}
