package com.backend.auth.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.backend.auth.dto.request.LoginReqDto;
import com.backend.auth.dto.request.RegisterReqDto;
import com.backend.auth.dto.response.LoginRespDto;
import com.backend.auth.dto.response.RegisterRespDto;
import com.backend.common.exception.ResourceNotFoundException;
import com.backend.common.exception.UserAlreadyExistsException;
import com.backend.security.CustomUserDetailsImpl;
import com.backend.security.JwtUtils;
import com.backend.user.entity.Role;
import com.backend.user.entity.User;
import com.backend.user.repository.RoleRepository;
import com.backend.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService /* ~~(Could not parse as Java)~~> */ {

	private final JwtUtils jwtUtils;

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	private final RoleRepository roleRepository;
	private final AuthenticationManager authenticationManager;

	// 1. B.L. for register a user

	@Override
	@Transactional
	public RegisterRespDto registerUser(@Valid RegisterReqDto request) {

		log.info("Attempting to register new User with email : {}", request.getEmail());

		String trimEmail = request.getEmail().trim().toLowerCase();
		
		if (userRepository.existsByEmail(trimEmail)) {
			throw new UserAlreadyExistsException("User with Email : ", trimEmail);
		}
		if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
			throw new UserAlreadyExistsException("User with Phone Number : ", request.getPhoneNumber());
		}

		/*
		 * After: 1. Look up the Role row by name ("ROLE_CUSTOMER", etc.) 2. Throw a
		 * clear error if the role name is not seeded yet 3. Add it to the User's roles
		 * Set
		 */
		Role role = roleRepository.findByRoleName(request.getRoleName())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Role not found in DB " + request.getRoleName(), null));

		String encodedPassword = passwordEncoder.encode(request.getPassword());

		User user = User.builder().fullName(request.getFullName()).email(trimEmail).password(encodedPassword)
				.phoneNumber(request.getPhoneNumber()).dateOfBirth(request.getDateOfBirth()).enabled(true)
				.accountLocked(false).build();

		
		user.getRoles().add(role);

		User saved = userRepository.save(user);

		log.info("User successfully registered with email : {}", request.getEmail());

		/*
		 * CHANGED — map saved.getRoles() → Set<String> of role names for the response.
		 *
		 * Before: .userRole(saved.getUserRole())
		 *
		 * After: .roles(Set of role name strings extracted from the Role entities)
		 */
		Set<String> roleNames = saved.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet());

		return RegisterRespDto.builder().id(saved.getId()).fullName(saved.getFullName()).email(saved.getEmail())
				.phoneNumber(saved.getPhoneNumber()).dateOfBirth(saved.getDateOfBirth()).roles(roleNames).build();
	}

	// 2. B.L. for login a user

	@Override
	public LoginRespDto authenticateUser(@Valid LoginReqDto request) {
		
		String normalizedEmail = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

		UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
				normalizedEmail, request.getPassword());

		log.info("******** Before auth {} ", authenticationToken.isAuthenticated()); // false

		Authentication fullyAuthenticated = authenticationManager.authenticate(authenticationToken);

		log.info("******** After succesful  auth {}", fullyAuthenticated.isAuthenticated()); // true
		log.info("******** Contents of auth {} ", fullyAuthenticated.getPrincipal());// custom user details

		CustomUserDetailsImpl user = (CustomUserDetailsImpl) fullyAuthenticated.getPrincipal();

		List<String> roles = new ArrayList<>();

		if (fullyAuthenticated != null && fullyAuthenticated.getAuthorities() != null) {
		    for (GrantedAuthority authority : fullyAuthenticated.getAuthorities()) {
		        roles.add(authority.getAuthority());
		    }
		}

		return new LoginRespDto(user.getUserId(), user.getFullName(), roles, jwtUtils.generateJWT(user));
	}
	
	@Override
	public Map<String, String> logout() {
	    SecurityContextHolder.clearContext();
	    log.info("User logged out — SecurityContextHolder cleared for this request thread");
	    return Map.of("message", "Logged out successfully");
	}
}

/*
*1 -> /auth/login controller hit 
*2 -> authenticateUser() inside auth service layer is called 
*3 --> inside the auth service layer 
*3.1-> create userNameAuthToken obj 
*3.2-> create authManager obj and call the authenticate method 
*3.3-> it authenticate method return us fully authenticate object
*3.4-> extract user detail from fully authenticate object getCredentials()
*3.5-> generate a token for userDetails 
*4--> create response dto obj initialise it and send back the response to the controller 
*/
