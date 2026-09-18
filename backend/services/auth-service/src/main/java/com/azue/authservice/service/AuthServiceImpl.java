package com.azue.authservice.service;

import com.azue.authservice.config.jwt.JwtService;
import com.azue.authservice.domain.entity.User;
import com.azue.authservice.dto.request.LoginRequest;
import com.azue.authservice.dto.request.RegisterRequest;
import com.azue.authservice.dto.response.AuthResponse;
import com.azue.authservice.exception.custom.DuplicateResourceException;
import com.azue.authservice.exception.custom.InvalidTokenException;
import com.azue.authservice.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String access = jwtService.generateAccessToken(userDetails);
        String refresh = jwtService.generateRefreshToken(userDetails);
        return new AuthResponse(access, refresh);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newAccess = jwtService.generateAccessToken(userDetails);
        /*
         * NOTE: Refresh rotation is disabled. In real production scenarios it should be enabled.
         * String newRefreshToken = jwtService.generateRefreshToken(userDetails);
         * return new AuthResponse(newAccessToken, newRefreshToken);
         */
        return new AuthResponse(newAccess, refreshToken);
    }

    @Override
    @Transactional()
    public User register(RegisterRequest request) {
        if (authRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }
        User user = User.create(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        return authRepository.save(user);
//        return login(new LoginRequest(request.email(), request.password()));
    }


}
