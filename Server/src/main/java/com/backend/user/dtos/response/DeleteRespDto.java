package com.backend.user.dtos.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteRespDto {
	private String message;
	
	private Long id;
	
	private LocalDateTime timeStamp;
}
