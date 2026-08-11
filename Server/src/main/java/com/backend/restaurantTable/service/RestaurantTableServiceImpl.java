package com.backend.restaurantTable.service;


import com.backend.common.exception.ResourceNotFoundException;
import com.backend.restaurant.entity.Restaurant;
import com.backend.restaurant.repository.RestaurantRepository;
import com.backend.restaurantTable.dto.request.RestaurantTableStatusUpdateDto;
import com.backend.restaurantTable.dto.request.RestaurantTableUpdateRequestDto;
import com.backend.restaurantTable.dto.response.RestaurantTableResponseDto;
import com.backend.restaurantTable.entity.RestaurantTable;
import com.backend.restaurantTable.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link RestaurantTableService} providing management operations
 * for restaurant tables, including table creation, retrieval, updates, status changes, and deletion.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RestaurantTableServiceImpl implements RestaurantTableService {

    private final RestaurantTableRepository restaurantTableRepository;
    private final RestaurantRepository restaurantRepository;

    
    /**
     * Retrieves all restaurant tables across all restaurants in the system.
     *
     * @return a list of {@link RestaurantTableResponseDto} representing all existing tables
     */
    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTableResponseDto> getAllTables() {
        log.info("Fetching all restaurant tables across system");
        List<RestaurantTableResponseDto> tables = restaurantTableRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .toList();

        log.info("Successfully retrieved {} table(s)", tables.size());
        return tables;
    }

    /**
     * Fetches details of a specific table by table ID, ensuring it belongs to the specified restaurant ID.
     *
     * @param restId the ID of the restaurant owning the table
     * @param tId the ID of the table to retrieve
     * @return {@link RestaurantTableResponseDto} details of the requested table
     * @throws ResourceNotFoundException if no matching table is found under the specified restaurant
     */
    @Override
    @Transactional(readOnly = true)
    public RestaurantTableResponseDto getTableById(Long restId, Long tId) {
        log.info("Fetching table ID: {} for restaurant ID: {}", tId, restId);

        RestaurantTable table = restaurantTableRepository.findByIdAndRestaurantId(tId, restId)
                .orElseThrow(() -> {
                    log.warn("Table ID: {} not found for restaurant ID: {}", tId, restId);
                    return new ResourceNotFoundException("Table not found with ID: " + tId + " for Restaurant ID: ", restId);
                });

        log.info("Successfully retrieved table ID: {} for restaurant ID: {}", tId, restId);
        return mapToResponseDto(table);
    }

    /**
     * Updates an existing table's details (table number, seating capacity, location type)
     * after validating hard ownership between table and restaurant.
     *
     * @param restId the ID of the restaurant owning the table
     * @param tId the ID of the table to update
     * @param dto payload containing updated table parameters
     * @return {@link RestaurantTableResponseDto} containing updated table details
     * @throws ResourceNotFoundException if table does not exist under the specified restaurant
     */
    @Override
    @Transactional
    public RestaurantTableResponseDto updateTable(Long restId, Long tId, RestaurantTableUpdateRequestDto dto) {
        log.info("Updating table ID: {} for restaurant ID: {}", tId, restId);

        // 1. Fetch table with hard ownership validation (verifies tId belongs to restId)
        RestaurantTable table = restaurantTableRepository.findByIdAndRestaurantId(tId, restId)
                .orElseThrow(() -> {
                    log.warn("Table update failed: Table ID: {} not found under restaurant ID: {}", tId, restId);
                    return new ResourceNotFoundException("Table not found with ID: " + tId + " under Restaurant ID: ", restId);
                });

        // 2. Mutate fields (Dirty Checking will handle DB persist on transaction commit)
        table.setTableNumber(dto.getTableNumber());
        table.setSeatingCapacity(dto.getSeatingCapacity());
        table.setLocationType(dto.getLocationType());

        log.info("Successfully updated table ID: {} for restaurant ID: {}", tId, restId);

        // 3. Map updated managed entity to DTO directly inside active transaction
        return mapToResponseDto(table);
    }
    
    /**
     * Updates the status of a specific restaurant table (e.g. AVAILABLE, OCCUPIED, RESERVED).
     *
     * @param restId the ID of the restaurant owning the table
     * @param tId the ID of the table whose status is to be updated
     * @param dto payload containing the new table status
     * @return {@link RestaurantTableResponseDto} containing updated table details
     * @throws ResourceNotFoundException if table does not exist under the specified restaurant
     */
    @Override
    @Transactional
    public RestaurantTableResponseDto updateTableStatus(Long restId, Long tId, RestaurantTableStatusUpdateDto dto) {
        log.info("Updating status for table ID: {} under restaurant ID: {} to status: {}", tId, restId, dto.getTableStatus());

        RestaurantTable table = fetchTableByRestaurantAndId(restId, tId);

        // Mutate status field - Hibernate dirty checking handles database write back
        table.setTableStatus(dto.getTableStatus());

        log.info("Successfully updated status for table ID: {} under restaurant ID: {} to: {}", tId, restId, dto.getTableStatus());
        return mapToResponseDto(table);
    }

    /**
     * Retrieves all tables associated with a given restaurant ID.
     *
     * @param restaurantId the ID of the target restaurant
     * @return a list of {@link RestaurantTableResponseDto} for the specified restaurant
     * @throws ResourceNotFoundException if the specified restaurant does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTableResponseDto> getRestaurantTables(Long restaurantId) {
        log.info("Fetching all tables for restaurant ID: {}", restaurantId);

        Restaurant restaurant = findRestaurantOrThrow(restaurantId);

        List<RestaurantTableResponseDto> tables = restaurantTableRepository.findByRestaurant(restaurant).stream()
                .map(this::mapToResponseDto)
                .toList();

        log.info("Successfully retrieved {} table(s) for restaurant ID: {}", tables.size(), restaurantId);
        return tables;
    }

    // ---- Helper Methods ----

    /**
     * Helper method to locate a table by ID or throw {@link ResourceNotFoundException}.
     *
     * @param tableId the ID of the table to locate
     * @return {@link RestaurantTable} entity
     */
    @SuppressWarnings("unused")
    private RestaurantTable findTableOrThrow(Long tableId) {
        return restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> {
                    log.warn("Table lookup failed for table ID: {}", tableId);
                    return new ResourceNotFoundException("Table not found with id: ", tableId);
                });
    }

    /**
     * Helper method to locate a restaurant by ID or throw {@link ResourceNotFoundException}.
     *
     * @param restaurantId the ID of the restaurant to locate
     * @return {@link Restaurant} entity
     */
    private Restaurant findRestaurantOrThrow(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    log.warn("Restaurant lookup failed for restaurant ID: {}", restaurantId);
                    return new ResourceNotFoundException("Restaurant not found with id: ", restaurantId);
                });
    }

    /**
     * Private helper method to centralize lookup & ownership validation.
     *
     * @param restId the restaurant ID
     * @param tId the table ID
     * @return {@link RestaurantTable} entity
     */
    private RestaurantTable fetchTableByRestaurantAndId(Long restId, Long tId) {
        return restaurantTableRepository.findByIdAndRestaurantId(tId, restId)
                .orElseThrow(() -> {
                    log.warn("Table lookup failed: Table ID: {} not found for restaurant ID: {}", tId, restId);
                    return new ResourceNotFoundException("Table not found with ID: " + tId + " for Restaurant ID: ", restId);
                });
    }

    /**
     * Private helper method to map a {@link RestaurantTable} entity to {@link RestaurantTableResponseDto}.
     * Runs inside the active transaction so LAZY associations (e.g., restaurant) are fetchable.
     *
     * @param table the entity to convert
     * @return {@link RestaurantTableResponseDto}
     */
    private RestaurantTableResponseDto mapToResponseDto(RestaurantTable table) {
        return RestaurantTableResponseDto.builder()
                .tableId(table.getId())
                .restaurantId(table.getRestaurant().getId())
                .restaurantName(table.getRestaurant().getName())
                .tableNumber(table.getTableNumber())
                .seatingCapacity(table.getSeatingCapacity())
                .locationType(table.getLocationType())
                .tableStatus(table.getTableStatus())
                .createdOn(table.getCreatedOn())
                .lastUpdated(table.getLastUpdated())
                .ratePerSeat(table.getRatePerSeat())   // Phase 3: expose rate for billing
                .build();
    }
}
