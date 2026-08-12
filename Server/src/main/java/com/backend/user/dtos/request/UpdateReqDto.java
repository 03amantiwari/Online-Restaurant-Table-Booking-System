package com.backend.user.dtos.request;

import com.backend.user.entity.DietPreference;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReqDto {
	
	@Size(max = 80 , message = "Name must be under 80 characters")
	private String fullName;
	
	@Size(max = 80,message = "Phone Number Too Long")
	private String phoneNumber;
	
	
	//Customer 
	private DietPreference dietPreference;
	
	
	//owner
	private String address;
}
