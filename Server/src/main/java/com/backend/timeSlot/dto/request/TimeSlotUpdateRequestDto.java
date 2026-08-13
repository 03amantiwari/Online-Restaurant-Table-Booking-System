package com.backend.timeSlot.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

// Note: no restaurantId here on purpose - a time slot shouldn't be reassigned
// to a different restaurant through a general update (same convention as
// RestaurantTableUpdateRequestDto / RestaurantUpdateRequestDto).
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotUpdateRequestDto {

    @NotBlank(message = "Label is required")
    private String label;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Active flag is required")
    private Boolean active;

    @Min(value = 1, message = "Max covers must be greater than 0")
    private Integer maxCovers;
}
