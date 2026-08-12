package com.backend.security;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.user.entity.User;
import com.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		log.info("********* in load user by user name :{}", userName);
		
		String normalizedEmail = (userName != null) ? userName.trim().toLowerCase() : "";
		
		 User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
		            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userName));

		return new CustomUserDetailsImpl(user.getId(),user.getFullName(), user.getEmail(), user.getPassword(),
				user.getRoles());
	}

}
