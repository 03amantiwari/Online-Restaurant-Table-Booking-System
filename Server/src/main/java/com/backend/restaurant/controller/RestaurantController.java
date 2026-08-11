package com.backend.restaurant.controller;

import com.backend.restaurant.dto.request.RestaurantCreateRequestDto;
import com.backend.restaurant.dto.response.RestaurantResponseDto;
import com.backend.restaurant.service.RestaurantService;
import com.backend.restaurantTable.dto.response.RestaurantTableResponseDto;
import com.backend.restaurantTable.service.RestaurantTableService;
import com.backend.timeSlot.dto.response.TimeSlotResponseDto;
import com.backend.timeSlot.service.TimeSlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

	private final RestaurantService restaurantService;
	private final RestaurantTableService restaurantTableService;
	private final TimeSlotService timeSlotService;

	// ── Restaurant CRUD ───────────────────────────────────────────────────────

	/*
	 * 1. POST /restaurants — single-call cascading restaurant creation
	 * access  : ROLE_OWNER (JWT required)
	 * payload : RestaurantCreateRequestDto (includes tables[] + timeSlots[])
	 * success : 201 Created with RestaurantResponseDto
	 */
	@PostMapping
	public ResponseEntity<RestaurantResponseDto> addRestaurant(
			@Valid @RequestBody RestaurantCreateRequestDto dto,
			@AuthenticationPrincipal Long ownerUserId) {
		RestaurantResponseDto created = restaurantService.addRestaurant(dto, ownerUserId);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	/*
	 * 2. GET /restaurants — public listing (only active=true restaurants)
	 * Soft-deleted restaurants (active=false) are excluded by the service layer.
	 */
	@GetMapping
	public ResponseEntity<List<RestaurantResponseDto>> getAllRestaurants() {
		return ResponseEntity.ok(restaurantService.getAllRestaurants());
	}

	/*
	 * 3. GET /restaurants/{id} — single restaurant detail (public)
	 */
	@GetMapping("/{id}")
	public ResponseEntity<RestaurantResponseDto> getRestaurantById(@PathVariable Long id) {
		return ResponseEntity.ok(restaurantService.getRestaurantById(id));
	}

	// ── Status & Visibility Controls ─────────────────────────────────────────

	/*
	 * 4. PATCH /restaurants/{id}/toggle-status — flip OPEN/CLOSED
	 * access  : ROLE_OWNER
	 * Toggles the operational open/closed status (different from soft-delete).
	 */
	@PatchMapping("/{id}/toggle-status")
	public ResponseEntity<RestaurantResponseDto> toggleRestaurantStatus(@PathVariable Long id) {
		return ResponseEntity.ok(restaurantService.toggleStatus(id));
	}

	/*
	 * 5. PATCH /restaurants/{id}/soft-delete — hide from customers
	 * access  : ROLE_OWNER
	 * Sets active=false. Restaurant disappears from Home page + search.
	 * No DB rows are deleted — owner can restore anytime.
	 */
	@PatchMapping("/{id}/soft-delete")
	public ResponseEntity<RestaurantResponseDto> softDeleteRestaurant(@PathVariable Long id) {
		return ResponseEntity.ok(restaurantService.softDelete(id));
	}

	/*
	 * 6. PATCH /restaurants/{id}/restore — bring restaurant back
	 * access  : ROLE_OWNER
	 * Sets active=true. Restaurant reappears on Home page immediately.
	 */
	@PatchMapping("/{id}/restore")
	public ResponseEntity<RestaurantResponseDto> restoreRestaurant(@PathVariable Long id) {
		return ResponseEntity.ok(restaurantService.restore(id));
	}

	// ── Tables & Timeslots (read-only — used by booking page) ────────────────

	/*
	 * 7. GET /restaurants/{restId}/tables — all tables for this restaurant
	 * public access — used by BookTable.jsx on mount
	 */
	@GetMapping("/{restId}/tables")
	public ResponseEntity<List<RestaurantTableResponseDto>> getRestaurantTables(
			@PathVariable Long restId) {
		return ResponseEntity.ok(restaurantTableService.getRestaurantTables(restId));
	}

	/*
	 * 8. GET /restaurants/{restId}/timeslots — all time slots for this restaurant
	 * public access — used by BookTable.jsx on mount
	 */
	@GetMapping("/{restId}/timeslots")
	public ResponseEntity<List<TimeSlotResponseDto>> getRestaurantSlots(
			@PathVariable Long restId) {
		return ResponseEntity.ok(timeSlotService.getRestaurantSlots(restId));
	}

	/*
	 * REMOVED (dead endpoints — never called by frontend):
	 *   PUT  /{id}                          updateRestaurant
	 *   DELETE /{id}                        deleteRestaurant (replaced by soft-delete)
	 *   PUT  /{restId}/tables/{tId}         updateTable
	 *   PUT  /{restId}/tables/{tId}/status  updateTableStatus
	 *   PUT  /{restId}/timeslots/{tId}      updateSlot
	 */
}
