package com.backend.restaurant.dto.request;

import com.backend.restaurantTable.entity.RestaurantTable.LocationType;
import jakarta.validation.constraints.Min;
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
public class RestaurantTableCreateDto {

    @NotNull(message = "Table number is required")
    @Positive(message = "Table number must be a positive integer")
    private Integer tableNumber;

    @NotNull(message = "Seating capacity is required")
    @Min(value = 1, message = "Seating capacity must be at least 1")
    private Integer seatingCapacity;

    @NotNull(message = "Location type is required (INDOOR, OUTDOOR, WINDOW, ROOFTOP, PRIVATE)")
    private LocationType locationType;

    @NotNull(message = "Rate per seat is required")
    @Positive(message = "Rate per seat must be greater than zero")
    private Double ratePerSeat;
}