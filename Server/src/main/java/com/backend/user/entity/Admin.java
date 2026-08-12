package com.backend.user.entity;

import jakarta.persistence.Entity;
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
@Table(name = "admin")
@ToString(callSuper = true , exclude = "user")
public class Admin extends BaseUser{

	@OneToOne
	@JoinColumn(name = "admin_id",nullable = false)
	@MapsId
	private User user;
}
