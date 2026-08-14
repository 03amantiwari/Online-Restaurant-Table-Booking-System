package com.backend.timeSlot.service;

import com.backend.timeSlot.dto.request.TimeSlotCreateRequestDto;
import com.backend.timeSlot.dto.request.TimeSlotUpdateRequestDto;
import com.backend.timeSlot.dto.response.TimeSlotResponseDto;

import java.util.List;

public interface TimeSlotService {

    TimeSlotResponseDto addTimeSlot(TimeSlotCreateRequestDto dto);

    List<TimeSlotResponseDto> getAllSlots();

    TimeSlotResponseDto updateSlot(Long restId, Long tId, TimeSlotUpdateRequestDto dto);

    void deleteSlot(Long restId, Long tId);

    List<TimeSlotResponseDto> getRestaurantSlots(Long restaurantId);
}
