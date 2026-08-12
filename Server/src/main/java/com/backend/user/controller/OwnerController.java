package com.backend.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.backend.user.service.OwnerService;
import com.backend.user.dtos.request.UpdateReqDto;

@RestController
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

	private final OwnerService ownerService;

	// GET /owner/{id} — owner views their own profile
	@GetMapping("/{oId}")
	public ResponseEntity<?> getOwner(@PathVariable Long oId) {
		return ResponseEntity.ok(ownerService.getOwnerDetails(oId));
	}

	// PUT /owner/{id} — owner updates their own profile
	@PutMapping("/{oId}")
	public ResponseEntity<?> updateOwner(@PathVariable Long oId,
			@RequestBody UpdateReqDto dto) {
		return ResponseEntity.ok(ownerService.updateOwnerDetails(oId, dto));
	}

	/*
	 * REMOVED (dead endpoint — never called by frontend):
	 *   DELETE /owner/{id}   deleteOwner
	 */
}
