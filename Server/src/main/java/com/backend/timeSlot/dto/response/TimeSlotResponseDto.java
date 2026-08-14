package com.backend.timeSlot.dto.response;

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
public class TimeSlotResponseDto {

    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String label;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean active;
    private Integer maxCovers;
}
