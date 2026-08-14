package com.backend.timeSlot.service;

import com.backend.common.exception.BusinessRuleViolationException;
import com.backend.common.exception.ResourceNotFoundException;
import com.backend.restaurant.entity.Restaurant;
import com.backend.restaurant.repository.RestaurantRepository;
import com.backend.timeSlot.dto.request.TimeSlotCreateRequestDto;
import com.backend.timeSlot.dto.request.TimeSlotUpdateRequestDto;
import com.backend.timeSlot.dto.response.TimeSlotResponseDto;
import com.backend.timeSlot.entity.TimeSlot;
import com.backend.timeSlot.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeSlotServiceImpl implements TimeSlotService {

	private final TimeSlotRepository timeSlotRepository;
	private final RestaurantRepository restaurantRepository;

	@Override
	@Transactional
	public TimeSlotResponseDto addTimeSlot(TimeSlotCreateRequestDto dto) {
		Restaurant restaurant = findRestaurantOrThrow(dto.getRestaurantId());
		validateTimeRange(dto.getStartTime(), dto.getEndTime());

		// active defaults to true via @PrePersist when not supplied
		TimeSlot slot = TimeSlot.builder().restaurant(restaurant).label(dto.getLabel()).startTime(dto.getStartTime())
				.endTime(dto.getEndTime()).active(dto.getActive()).maxCovers(dto.getMaxCovers()).build(); 

		return mapToResponseDto(timeSlotRepository.save(slot));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimeSlotResponseDto> getAllSlots() {
		return timeSlotRepository.findAll().stream().map(this::mapToResponseDto).toList();
	}

	@Override
	@Transactional
	public TimeSlotResponseDto updateSlot(Long restId, Long id, TimeSlotUpdateRequestDto dto) {
		log.info("Updating time slot ID: {} for restaurant ID: {}", id, restId);

		// 1. Fetch slot with hard ownership check (verifies slot 'id' belongs to
		// 'restId')
		TimeSlot slot = timeSlotRepository.findByIdAndRestaurantId(id, restId).orElseThrow(
				() -> new ResourceNotFoundException("Time slot not found with ID: " + id + " under Restaurant ID: ",
						restId));

		// 2. Business Validation: Ensure Start Time is before End Time
		validateTimeRange(dto.getStartTime(), dto.getEndTime());


		// 4. Update fields (Hibernate Dirty Checking will issue UPDATE query on
		// transaction commit)
		slot.setLabel(dto.getLabel());
		slot.setStartTime(dto.getStartTime());
		slot.setEndTime(dto.getEndTime());
		slot.setActive(dto.getActive());
		slot.setMaxCovers(dto.getMaxCovers());

		log.info("Successfully updated time slot ID: {} for restaurant ID: {}", id, restId);

		// 5. Map to DTO directly inside active transaction
		return mapToResponseDto(slot);
	}

	@Override
	@Transactional
	public void deleteSlot(Long restId, Long id) {
		log.info("Attempting to delete time slot ID: {} for restaurant ID: {}", id, restId);

		// 1. Fetch time slot with strict tenant ownership check
		TimeSlot slot = timeSlotRepository.findByIdAndRestaurantId(id, restId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Time slot not found with ID: " + id + " under Restaurant ID: " , restId));

		// 2. Safeguard against deleting time slots with future active bookings
		// boolean hasUpcomingBookings =
		// reservationRepository.existsByTimeSlotIdAndStatusIn(id,
		// List.of(Status.BOOKED, Status.CONFIRMED));
		// if (hasUpcomingBookings) {
		// throw new ResourceInUseException("Cannot delete time slot ID " + id + "
		// because it has upcoming active reservations.");
		// }

		// 3. Perform hard deletion
		timeSlotRepository.delete(slot);

		log.info("Successfully deleted time slot ID: {} from restaurant ID: {}", id, restId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TimeSlotResponseDto> getRestaurantSlots(Long restaurantId) {
		Restaurant restaurant = findRestaurantOrThrow(restaurantId);
		return timeSlotRepository.findByRestaurant(restaurant).stream().map(this::mapToResponseDto).toList();
	}

	// ---- helpers ----


	private Restaurant findRestaurantOrThrow(Long restaurantId) {
		return restaurantRepository.findById(restaurantId)
				.orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: ", restaurantId));
	}

	private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
		if (!endTime.isAfter(startTime)) {
			throw new BusinessRuleViolationException("End time must be after start time");
		}
	}

	// Mapped manually (see RestaurantTableServiceImpl for why: STRICT ModelMapper
	// won't flatten nested associations like restaurant.name -> restaurantName).
	// Runs inside the @Transactional method so the LAZY restaurant association is
	private TimeSlotResponseDto mapToResponseDto(TimeSlot slot) {
		return TimeSlotResponseDto.builder().id(slot.getId()).restaurantId(slot.getRestaurant().getId())
				.restaurantName(slot.getRestaurant().getName()).label(slot.getLabel()).startTime(slot.getStartTime())
				.endTime(slot.getEndTime()).active(slot.getActive()).maxCovers(slot.getMaxCovers()).build();
	}
}
