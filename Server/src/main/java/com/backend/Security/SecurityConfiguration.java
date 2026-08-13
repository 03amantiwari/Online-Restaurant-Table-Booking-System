package com.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
	
	@Value("${cors.allowed-origins}")
	private String corsAllowOrigin;

	private final JwtVerificationFilter jwtFilter;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		// 1. Disable CSRF for Stateless REST APIs
		http.csrf(csrf -> csrf.disable());

		// 2. Enable CORS configuration
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

		// 3. Disable default Login Mechanisms
		http.formLogin(form -> form.disable());
		http.httpBasic(basic -> basic.disable());

		// 4. Stateless Session Management
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// 5. Complete Authorization Rules
		http.authorizeHttpRequests(request -> request
				// Public: Permit all OPTIONS preflight requests globally
				
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// Public Endpoints
				.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/auth/**").permitAll()
				.requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
            	// Secure all other actuator endpoints (metrics, env, heapdump)
            	.requestMatchers("/actuator/**").hasRole("ADMIN")

				// Public: Read-only restaurant endpoints
				.requestMatchers(HttpMethod.GET, "/restaurants", "/restaurants/*").permitAll()
				.requestMatchers(HttpMethod.GET, "/restaurants/*/tables", "/restaurants/*/timeslots").permitAll()

				// Public: Booking endpoints
				.requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/bookings/my").permitAll()
				.requestMatchers(HttpMethod.PUT, "/api/bookings/*/cancel").permitAll()
				// Owner-controlled booking status update
				.requestMatchers(HttpMethod.PUT, "/api/bookings/*/status").authenticated()

				// Dashboard profile endpoints — authenticated users
				.requestMatchers(HttpMethod.GET, "/customer/*").authenticated()
				.requestMatchers(HttpMethod.PUT, "/customer/*").authenticated()
				.requestMatchers(HttpMethod.GET, "/owner/*").authenticated().requestMatchers(HttpMethod.PUT, "/owner/*")
				.authenticated().requestMatchers(HttpMethod.GET, "/admin/owner").authenticated()
				.requestMatchers(HttpMethod.GET, "/admin/customer").authenticated()
				.requestMatchers(HttpMethod.GET, "/api/restaurants/*/bookings").authenticated()

				// Protected Role-based Endpoints - Customer
				.requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "ADMIN")

				// Protected Role-based Endpoints - Owner
				.requestMatchers(HttpMethod.POST, "/restaurants/**").hasAnyRole("OWNER")
				.requestMatchers(HttpMethod.PUT, "/restaurants/**").hasAnyRole("OWNER")
				.requestMatchers(HttpMethod.PATCH, "/restaurants/**").hasAnyRole("OWNER")
				.requestMatchers(HttpMethod.DELETE, "/restaurants/**").hasAnyRole("OWNER").requestMatchers("/owner/**")
				.hasAnyRole("OWNER", "ADMIN").requestMatchers("/api/restaurants/**").hasAnyRole("OWNER", "ADMIN")

				// Protected Role-based Endpoints - Admin
				.requestMatchers("/admin/**", "/user").hasRole("ADMIN")

				// Any other request must be authenticated
				.anyRequest().authenticated());


		// Add JwtVerificationFilter before UsernamePasswordAuthenticationFilter
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        if (corsAllowOrigin != null && !corsAllowOrigin.isBlank()) {
            List<String> origins = Arrays.stream(corsAllowOrigin.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            config.setAllowedOriginPatterns(origins);
        } else {
            config.setAllowedOriginPatterns(List.of("*"));
        }
        

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Allow all headers + expose Private Network header
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}