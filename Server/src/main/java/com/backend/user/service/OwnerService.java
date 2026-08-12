package com.backend.user.service;

import com.backend.user.dtos.request.UpdateReqDto;
import com.backend.user.dtos.response.DeleteRespDto;
import com.backend.user.dtos.response.UserGetRespDto;

import jakarta.validation.Valid;

public interface OwnerService {

	UserGetRespDto getOwnerDetails(Long oId);

	UserGetRespDto updateOwnerDetails(Long oId, @Valid UpdateReqDto request);

	DeleteRespDto deleteCustomerDetails(Long oId);

}
