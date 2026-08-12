package com.backend.user.service;

import java.util.List;

import com.backend.user.dtos.response.UserGetRespDto;


public interface UserService {

	List<UserGetRespDto> getAllUserDetails();

}
