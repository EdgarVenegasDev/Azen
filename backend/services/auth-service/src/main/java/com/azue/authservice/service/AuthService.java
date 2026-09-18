package com.azue.authservice.service;

import com.azue.authservice.domain.entity.User;
import com.azue.authservice.dto.request.LoginRequest;
import com.azue.authservice.dto.request.RegisterRequest;
import com.azue.authservice.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    User register(RegisterRequest request);
}
