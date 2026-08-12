package com.backend.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;



@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "customers")
@ToString(callSuper = true,exclude="user")
public class Customer extends BaseUser{
	
	@Enumerated(EnumType.STRING)
	@Column(name = "diet_prefer")
	private DietPreference dietPreference;
	
	@OneToOne
	@JoinColumn(name = "cust_id",nullable = false)
	@MapsId
	private User user;
	
}
