package com.backend.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.backend.user.service.CustomerService;
import com.backend.user.dtos.request.UpdateReqDto;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerService customerService;

	// GET /customer/{id} — customer views their own profile
	@GetMapping("/{cId}")
	public ResponseEntity<?> getCustomer(@PathVariable Long cId) {
		return ResponseEntity.ok(customerService.getCustomerDetails(cId));
	}

	// PUT /customer/{id} — customer updates their own profile
	@PutMapping("/{cId}")
	public ResponseEntity<?> updateCustomer(@PathVariable Long cId,
			@RequestBody UpdateReqDto dto) {
		return ResponseEntity.ok(customerService.updateCustomerDetails(cId, dto));
	}

}
