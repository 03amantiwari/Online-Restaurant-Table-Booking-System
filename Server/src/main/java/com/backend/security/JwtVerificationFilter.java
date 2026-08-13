package com.backend.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtVerificationFilter extends OncePerRequestFilter {

	private final JwtUtils jwtUtils;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getServletPath();

		// In sabhi paths par JWT verification filter KO SKIP KARO
		return path.startsWith("/auth") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
				|| path.startsWith("/swagger-resources") || path.startsWith("/webjars");
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			String authHeader = request.getHeader("Authorization");

			if (authHeader != null && authHeader.startsWith("Bearer ")) {

				String jwt = authHeader.substring(7);

				log.info("*********** JWT {}", jwt);

				Claims payload = jwtUtils.verifyJwtAndExtractClaims(jwt);

				Long userId = payload.get("user_id", Long.class);
				@SuppressWarnings("unchecked")
				List<String> roles = payload.get("user_roles", List.class);

				List<SimpleGrantedAuthority> authorities = new ArrayList<>();

				if (roles != null) {
					for (String role : roles) {
						if (role != null) {
							authorities.add(new SimpleGrantedAuthority(role));
						}
					}
				}

				UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
						userId, null, authorities);

				SecurityContextHolder.getContext().setAuthentication(authenticationToken);
			}
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			log.error("JWT Authentication failed: {}", e.getMessage());
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// SC 401
			response.setContentType("application/json");
			response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + e.getMessage() + "\"}");
			return;
		}

	}

}
