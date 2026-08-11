package com.backend.restaurantTable.service;


import com.backend.restaurantTable.dto.request.RestaurantTableStatusUpdateDto;
import com.backend.restaurantTable.dto.request.RestaurantTableUpdateRequestDto;
import com.backend.restaurantTable.dto.response.RestaurantTableResponseDto;

import java.util.List;

/**
 * Service interface defining operations for managing restaurant tables.
 */
public interface RestaurantTableService {



    /**
     * Retrieves all tables across all restaurants.
     *
     * @return list of all restaurant tables
     */
    List<RestaurantTableResponseDto> getAllTables();

    /**
     * Retrieves a table by its ID and associated restaurant ID.
     *
     * @param restId restaurant ID
     * @param tId table ID
     * @return details of the matching table
     */
    RestaurantTableResponseDto getTableById(Long restId, Long tId);

    /**
     * Updates an existing table's information.
     *
     * @param restId restaurant ID
     * @param tId table ID
     * @param dto update parameters
     * @return details of the updated table
     */
    RestaurantTableResponseDto updateTable(Long restId, Long tId, RestaurantTableUpdateRequestDto dto);


 
    RestaurantTableResponseDto updateTableStatus(Long restId, Long tId, RestaurantTableStatusUpdateDto dto);

    /**
     * Retrieves all tables belonging to a specific restaurant.
     *
     * @param restaurantId target restaurant ID
     * @return list of tables for the specified restaurant
     */
    List<RestaurantTableResponseDto> getRestaurantTables(Long restaurantId);
}