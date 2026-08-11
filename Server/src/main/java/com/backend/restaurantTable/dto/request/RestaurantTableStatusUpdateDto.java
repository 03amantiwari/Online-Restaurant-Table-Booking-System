package com.backend.restaurantTable.dto.request;

import com.backend.restaurantTable.entity.RestaurantTable;
import jakarta.validation.constraints.NotNull;
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
public class RestaurantTableStatusUpdateDto {

    @NotNull(message = "Table status is required")
    private RestaurantTable.TableStatus tableStatus;
}