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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotCreateRequestDto {

    @NotNull(message = "Restaurant id is required")
    private Long restaurantId;

    @NotBlank(message = "Label is required")
    private String label;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    // Optional - defaults to true (active) via TimeSlot#onCreate if not supplied
    private Boolean active;

    @Min(value = 1, message = "Max covers must be greater than 0")
    private Integer maxCovers;
}
