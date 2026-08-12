package com.backend.auth.controller;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.auth.dto.request.LoginReqDto;
import com.backend.auth.dto.request.RegisterReqDto;
import com.backend.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
	private final AuthService authService;

	/*
	 * signup -> register user signin -> login / authenticate user
	 */

	/*
	 * 1 access - to every one DESC - add new the user(CUSTOMER || OWNER || ADMIN)
	 * URL - POST:http://host:port/auth.signup payload - {fullName , dateofbirth
	 * ,email,password ,phoneNumber , userRole ....} success - 200 ok with register
	 * response Dto failure - 400 Bad request
	 */

	@PostMapping("/signup")
	public ResponseEntity<?> userRegister(@RequestBody @Valid RegisterReqDto request) {
		log.info("in user register in " + request.getEmail());

		return ResponseEntity.ok(authService.registerUser(request));
	}

	/*
	 * 2 access - to every one DESC - to authenticate the user(CUSTOMER || OWNER ||
	 * ADMIN) URL - POST:http://host:port/auth/signin payload - {fullName ,
	 * dateofbirth ,email,password ,phoneNumber , userRole ....} success - 200 ok
	 * with register response Dto failure - 400 Bad request
	 */

	@PostMapping("/signin")
	public ResponseEntity<?> userSignIn(@RequestBody @Valid LoginReqDto request) {
		return ResponseEntity.ok(authService.authenticateUser(request));
	}

	/*
	 * 3 access - authenticated users only DESC - clears Spring Security context for
	 * this request thread URL - POST:http://host:port/auth/logout success - 200 ok
	 * with { message: "Logged out successfully" } Note: JWT is stateless -
	 * The client drops the token; the server clears its thread context.
	 */
	@PostMapping("/logout")
	public ResponseEntity<?> logout() {
		// Delegate to service — Single Responsibility: controller only routes HTTP
		return ResponseEntity.ok(authService.logout());
	}
}
