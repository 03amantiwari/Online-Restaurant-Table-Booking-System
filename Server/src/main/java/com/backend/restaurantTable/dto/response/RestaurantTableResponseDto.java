package com.backend.restaurantTable.dto.response;

import com.backend.restaurantTable.entity.RestaurantTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// DTO returned to frontend for every table — includes ratePerSeat for billing
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantTableResponseDto {

    private Long tableId;
    private Long restaurantId;
    private String restaurantName;
    private Integer tableNumber;
    private Integer seatingCapacity;
    private RestaurantTable.LocationType locationType;
    private RestaurantTable.TableStatus tableStatus;
    private LocalDateTime createdOn;
    private LocalDateTime lastUpdated;

    // Phase 3 addition: rate per seat used by frontend billing engine
    private Double ratePerSeat;
}
