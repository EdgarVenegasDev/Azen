package com.azue.authservice.service;

import com.azue.authservice.dto.request.LoginRequest;
import com.azue.authservice.dto.request.LogoutRequest;
import com.azue.authservice.dto.request.RefreshTokenRequest;
import com.azue.authservice.dto.request.RegisterRequest;
import com.azue.authservice.dto.response.AuthResponse;
import com.azue.authservice.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

}
