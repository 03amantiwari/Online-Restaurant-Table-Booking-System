package com.backend.restaurantTable.dto.request;

import com.backend.restaurantTable.entity.RestaurantTable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTableCreateRequestDto {

    @NotNull(message = "Restaurant id is required")
    private Long restaurantId;

    @NotNull(message = "Table number is required")
    @Positive(message = "Table number must be a positive number")
    private Integer tableNumber;

    @NotNull(message = "Seating capacity is required")
    @Positive(message = "Seating capacity must be greater than 0")
    private Integer seatingCapacity;

    @NotNull(message = "Location type is required")
    private RestaurantTable.LocationType locationType;
}