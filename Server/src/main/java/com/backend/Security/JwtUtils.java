package com.backend.security;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtils {
	@Value("${jwt.secret.key}")
	private String secret;
	
	@Value("${jwt.exp.time}")
	private long expTime;
	
	private SecretKey key;
	
	@PostConstruct
	public void init() {
		log.info("******* in init - generating symmetric secret key SHA 256");
		key=Keys.hmacShaKeyFor(secret.getBytes());
	}
	
	//logic to generate the token 
	public String generateJWT(CustomUserDetailsImpl userDetails) {
		Date now = new Date();
		Date expDate = new Date(now.getTime() + expTime);
		
		 List<String> roleNames = userDetails.getAuthorities().stream()
	                .map(GrantedAuthority::getAuthority)
	                .collect(Collectors.toList());
		
		return Jwts.builder() //create JWT builder 
				.subject(userDetails.getUsername())
				.issuedAt(now)
				.expiration(expDate)
				.claim("user_id",userDetails.getUserId())
				.claim("user_roles",roleNames)
				.signWith(key)
				.compact();
	}
	
	//logic to validate the token and extract the claims 
	public Claims verifyJwtAndExtractClaims(String jwt) {
		return Jwts.parser() // create builder to parse JWT -> 
				.verifyWith(key) //verifying signature -> extract header+payload from token
				.build()  //builds JWT parser  -> extract header+payload form new token header.payload.sign = new_Token 
				.parseSignedClaims(jwt)  //in case of invalid JWT - throws exception -> match the token && new token decide after this what to done
				.getPayload();   // extracting the claims
	}
	
}



























