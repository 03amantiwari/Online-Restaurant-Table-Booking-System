package com.backend.user.service;

import com.backend.user.dtos.request.UpdateReqDto;
import com.backend.user.dtos.response.DeleteRespDto;
import com.backend.user.dtos.response.UserGetRespDto;

import jakarta.validation.Valid;

public interface CustomerService {

	UserGetRespDto getCustomerDetails(Long cId);

	UserGetRespDto updateCustomerDetails(Long cId, @Valid UpdateReqDto request);

	DeleteRespDto deleteCustomerDetails(Long cId);

}
