package com.backend.auth.service;



import java.util.Map;

import com.backend.auth.dto.request.LoginReqDto;
import com.backend.auth.dto.request.RegisterReqDto;
import com.backend.auth.dto.response.LoginRespDto;
import com.backend.auth.dto.response.RegisterRespDto;

import jakarta.validation.Valid;

public interface AuthService {

	RegisterRespDto registerUser(@Valid RegisterReqDto request);

	LoginRespDto authenticateUser(LoginReqDto request);

	Map<String, String> logout();

}
