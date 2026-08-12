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
@Table(name = "owners")
@ToString(callSuper = true,exclude="user")
public class Owner extends BaseUser{
	
	@Column(name = "address" , length = 150,nullable = false)
	private String address;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "id_proof",nullable = false)
	private IdentityProof identityProof;
	
	@Column(name = "id_number",nullable = false,length = 20,unique = true)
	private String idNumber;
	
	@Column(name = "kyc_status")
	private Boolean kycStatus = false;
	
	@OneToOne
	@JoinColumn(name = "owner_id",nullable = false)
	@MapsId
	private User user;
	
}
