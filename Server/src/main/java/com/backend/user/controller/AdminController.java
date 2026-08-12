package com.backend.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.backend.user.service.AdminService;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final AdminService adminService;

	// GET /admin/customer — list all registered customers
	@GetMapping("/customer")
	public ResponseEntity<?> getAllCustomers() {
		return ResponseEntity.ok(adminService.getAllCustomerDetails());
	}

	// GET /admin/owner — list all registered owners
	@GetMapping("/owner")
	public ResponseEntity<?> getAllOwners() {
		return ResponseEntity.ok(adminService.getAllOwnerDetails());
	}

}
