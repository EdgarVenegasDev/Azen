package com.azue.authservice.mapper;

import com.azue.authservice.domain.entity.User;
import com.azue.authservice.dto.response.AuthResponse;
import com.azue.authservice.dto.response.RegisterResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    AuthResponse toResponse(User user);

    RegisterResponse toRegisterResponse(User user);
}
