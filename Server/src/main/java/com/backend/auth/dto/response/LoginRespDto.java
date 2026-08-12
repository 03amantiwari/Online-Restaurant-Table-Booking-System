package com.backend.auth.dto.response;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespDto {
	private Long id;
	
	private String fullName;
	
	private List<String> roles;
	
	private String jwt;

}
