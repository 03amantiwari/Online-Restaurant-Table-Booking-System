package com.backend.restaurantTable.dto.request;

import com.backend.restaurantTable.entity.RestaurantTable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Note: no restaurantId here on purpose - a table shouldn't be reassigned
// to a different restaurant through a general update.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTableUpdateRequestDto {

	@NotNull(message = "Restaurant Id is required")
	@Positive(message = "Table number must be a positive number")
	private Long restId;
	
    @NotNull(message = "Table number is required")
    @Positive(message = "Table number must be a positive number")
    private Integer tableNumber;

    @NotNull(message = "Seating capacity is required")
    @Positive(message = "Seating capacity must be greater than 0")
    private Integer seatingCapacity;

    @NotNull(message = "Location type is required")
    private RestaurantTable.LocationType locationType;
}