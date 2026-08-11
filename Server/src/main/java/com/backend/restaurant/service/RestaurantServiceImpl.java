package com.backend.restaurant.service;

import com.backend.common.exception.DuplicateResourceException;
import com.backend.common.exception.ResourceNotFoundException;
import com.backend.restaurant.dto.request.RestaurantCreateRequestDto;
import com.backend.restaurant.dto.request.RestaurantTableCreateDto;
import com.backend.restaurant.dto.request.TimeSlotCreateDto;
import com.backend.restaurant.dto.response.RestaurantResponseDto;
import com.backend.restaurant.dto.response.RestaurantUpdateRequestDto;
import com.backend.restaurant.entity.Restaurant;
import com.backend.restaurant.repository.RestaurantRepository;
import com.backend.restaurantTable.entity.RestaurantTable;
import com.backend.timeSlot.entity.TimeSlot;
import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public RestaurantResponseDto addRestaurant(RestaurantCreateRequestDto dto, Long ownerUserId) {
        log.info("Creating new restaurant setup for ownerUserId: {}", ownerUserId);

        // 1. Fetch logged-in Owner User from Database
        User owner = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " , ownerUserId));

        // 2. Guard Check: Unique Restaurant Name
        if (restaurantRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException(
                    "A restaurant named '" + dto.getName() + "' already exists");
        }

        // 3. Build Parent Restaurant
        Restaurant restaurant = Restaurant.builder()
                .ownerUser(owner) // 👈 Set securely from token
                .name(dto.getName())
                .city(dto.getCity())
                .address(dto.getAddress())
                .contactNumber(dto.getContactNumber())
                .contactEmail(dto.getContactEmail())
                .priceBand(dto.getPriceBand())
                .build();

        // 4. Attach Tables
        if (dto.getTables() != null && !dto.getTables().isEmpty()) {
            for (RestaurantTableCreateDto tableDto : dto.getTables()) {
                RestaurantTable table = RestaurantTable.builder()
                        .tableNumber(tableDto.getTableNumber())
                        .seatingCapacity(tableDto.getSeatingCapacity())
                        .locationType(tableDto.getLocationType())
                        .ratePerSeat(tableDto.getRatePerSeat())
                        .tableStatus(RestaurantTable.TableStatus.AVAILABLE)
                        .build();

                restaurant.addTable(table);
            }
        }

        // 5. Attach TimeSlots
        if (dto.getTimeSlots() != null && !dto.getTimeSlots().isEmpty()) {
            for (TimeSlotCreateDto slotDto : dto.getTimeSlots()) {
                TimeSlot timeSlot = TimeSlot.builder()
                        .label(slotDto.getLabel())
                        .startTime(slotDto.getStartTime())
                        .endTime(slotDto.getEndTime())
                        .maxCovers(slotDto.getMaxCovers())
                        .active(true)
                        .build();

                restaurant.addTimeSlot(timeSlot);
            }
        }

        // 6. Save Cascade
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant created successfully with id: {}", savedRestaurant.getId());

        return mapToResponseDto(savedRestaurant);
    }
    

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponseDto> getAllRestaurants() {
        /*
         * Filter by deleted=false — NOT by active.
         *
         * active=false means CLOSED (restaurant is temporarily closed).
         * A closed restaurant should still appear on the Home page so
         * customers can see it exists (even if bookings are unavailable).
         *
         * deleted=true means the owner has unlisted it completely.
         * Only deleted=false restaurants should show on the Home page.
         */
        return restaurantRepository.findAll()
                .stream()
                .filter(r -> !Boolean.TRUE.equals(r.getDeleted()))  // exclude soft-deleted
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponseDto getRestaurantById(Long id) {
        return mapToResponseDto(findRestaurantOrThrow(id));
    }

    @Override
    @Transactional
    public RestaurantResponseDto updateRestaurant(Long id, RestaurantUpdateRequestDto dto) {
        Restaurant restaurant = findRestaurantOrThrow(id);

        boolean nameChanged = !restaurant.getName().equals(dto.getName());
        if (nameChanged && restaurantRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new DuplicateResourceException(
                    "A restaurant named '" + dto.getName() + "' already exists");
        }

        restaurant.setName(dto.getName());
        restaurant.setCity(dto.getCity());
        restaurant.setAddress(dto.getAddress());
        restaurant.setContactNumber(dto.getContactNumber());
        restaurant.setContactEmail(dto.getContactEmail());
        restaurant.setPriceBand(dto.getPriceBand());
        restaurant.setActive(dto.getActive());

        return mapToResponseDto(restaurantRepository.save(restaurant));
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = findRestaurantOrThrow(id);
        restaurantRepository.delete(restaurant);
    }

    /*
     * toggleStatus — flips the `active` boolean on the Restaurant record.
     *
     * WHY a dedicated method instead of re-using updateRestaurant()?
     * updateRestaurant() requires the full request body (name, city, address...).
     * A toggle only needs the restaurant id — no request body at all.
     * Keeping it separate makes the intent clear and the API minimal.
     *
     * FLOW:
     *   1. Load restaurant or throw 404
     *   2. Flip active: true → false  OR  false → true
     *   3. Save and return updated DTO
     */
    @Override
    @Transactional
    public RestaurantResponseDto toggleStatus(Long id) {
        Restaurant restaurant = findRestaurantOrThrow(id);
        // Flip the boolean — if currently true set false, and vice versa
        restaurant.setActive(!Boolean.TRUE.equals(restaurant.getActive()));
        log.info("Restaurant id={} status toggled to active={}", id, restaurant.getActive());
        return mapToResponseDto(restaurantRepository.save(restaurant));
    }

    /*
     * softDelete — sets deleted=true.
     *
     * WHY deleted field and NOT the active field?
     * active = controls open/closed status (owner's daily operational toggle).
     * deleted = controls visibility to customers (unlisting decision).
     *
     * If we set active=false for soft-delete, then a CLOSED restaurant
     * would also be treated as deleted — wrong behaviour.
     * Using a separate deleted field keeps both meanings independent.
     */
    @Override
    @Transactional
    public RestaurantResponseDto softDelete(Long id) {
        Restaurant restaurant = findRestaurantOrThrow(id);
        restaurant.setDeleted(true);  // hide from customers — does NOT touch active
        log.info("Restaurant id={} soft-deleted (deleted=true)", id);
        return mapToResponseDto(restaurantRepository.save(restaurant));
    }

    // restore — sets deleted=false, restaurant reappears on Home page
    @Override
    @Transactional
    public RestaurantResponseDto restore(Long id) {
        Restaurant restaurant = findRestaurantOrThrow(id);
        restaurant.setDeleted(false);  // visible to customers again
        log.info("Restaurant id={} restored (deleted=false)", id);
        return mapToResponseDto(restaurantRepository.save(restaurant));
    }

    // ---- helpers ----

    private Restaurant findRestaurantOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " , id));
    }

    // Mapped manually (see RestaurantTableServiceImpl for why: STRICT ModelMapper
    // won't flatten nested associations like ownerUser.fullName -> ownerUserName).
    // Runs inside the @Transactional method so the LAZY ownerUser association is
    // still fetchable (open-in-view is disabled).
    private RestaurantResponseDto mapToResponseDto(Restaurant restaurant) {
        return RestaurantResponseDto.builder()
                .id(restaurant.getId())
                .ownerUserId(restaurant.getOwnerUser().getId())
                .ownerUserName(restaurant.getOwnerUser().getFullName())
                .name(restaurant.getName())
                .city(restaurant.getCity())
                .address(restaurant.getAddress())
                .contactNumber(restaurant.getContactNumber())
                .contactEmail(restaurant.getContactEmail())
                .priceBand(restaurant.getPriceBand())
                .active(restaurant.getActive())
                .deleted(restaurant.getDeleted())   // needed by frontend to split active/unlisted sections
                .createdAt(restaurant.getCreatedAt())
                .build();
    }
}
