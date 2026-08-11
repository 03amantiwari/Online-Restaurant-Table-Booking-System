package com.backend.restaurant.service;

import com.backend.restaurant.dto.request.RestaurantCreateRequestDto;
import com.backend.restaurant.dto.response.RestaurantResponseDto;
import com.backend.restaurant.dto.response.RestaurantUpdateRequestDto;

import java.util.List;

public interface RestaurantService {

    RestaurantResponseDto addRestaurant(RestaurantCreateRequestDto dto, Long ownerUserId);

    List<RestaurantResponseDto> getAllRestaurants();

    RestaurantResponseDto getRestaurantById(Long id);

    RestaurantResponseDto updateRestaurant(Long id, RestaurantUpdateRequestDto dto);

    void deleteRestaurant(Long id);

    // Flips restaurant active=true → false or false → true, returns updated DTO
    RestaurantResponseDto toggleStatus(Long id);

    // Soft delete — sets active=false (restaurant hidden from public, not deleted from DB)
    RestaurantResponseDto softDelete(Long id);

    // Restore — sets active=true (brings restaurant back to public listing)
    RestaurantResponseDto restore(Long id);
}
