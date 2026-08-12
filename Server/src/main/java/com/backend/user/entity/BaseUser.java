package com.backend.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@MappedSuperclass
@Getter
@Setter
@ToString

public class BaseUser {
	
	//Auto-generated primary key for each user
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;
	
	// used in audit purpose
	@CreationTimestamp
	@Column(name = "created_on",nullable = false,updatable = false)
	private LocalDateTime createdOn;
	
	@UpdateTimestamp
	@Column(name = "last_updated")
	private LocalDateTime lastUpdated; 
	
	//private LocalDateTime lastLoginAt;
	
	
}
