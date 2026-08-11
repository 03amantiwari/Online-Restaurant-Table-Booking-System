package com.backend.restaurantTable.repository;

import com.backend.booking.entity.Booking;
import com.backend.restaurant.entity.Restaurant;
import com.backend.restaurantTable.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

	Optional<RestaurantTable> findByIdAndRestaurantId(Long id, Long restaurantId);

	boolean existsByRestaurantIdAndId(Long restaurantId, Long id);

	boolean existsByRestaurantAndTableNumber(Restaurant restaurant, Integer tableNumber);

	boolean existsByRestaurantAndTableNumberAndIdNot(Restaurant restaurant, Integer tableNumber, Long id);

	List<RestaurantTable> findByRestaurant(Restaurant restaurant);

	List<RestaurantTable> findByRestaurantId(Long restaurantId);

	List<RestaurantTable> findByRestaurantAndTableStatus(Restaurant restaurant,
			RestaurantTable.TableStatus tableStatus);

	// ---- Customer: Available tables search ----

	@Query("""
			SELECT rt FROM RestaurantTable rt
			WHERE rt.restaurant.id = :restaurantId
			  AND rt.tableStatus = :availableStatus
			  AND rt.seatingCapacity >= :guestCount
			  AND rt.id NOT IN (
			      SELECT b.restaurantTable.id FROM Booking b
			      WHERE b.restaurant.id = :restaurantId
			        AND b.bookingDate = :bookingDate
			        AND b.timeSlot.id = :timeSlotId
			        AND b.status IN :activeStatuses
			  )
			""")
	List<RestaurantTable> findAvailableTables(@Param("restaurantId") Long restaurantId,
			@Param("guestCount") Integer guestCount, @Param("bookingDate") LocalDate bookingDate,
			@Param("timeSlotId") Long timeSlotId, @Param("availableStatus") RestaurantTable.TableStatus availableStatus,
			@Param("activeStatuses") List<Booking.BookingStatus> activeStatuses);
}