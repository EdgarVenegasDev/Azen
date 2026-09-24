package com.azue.authservice.service;

import com.azue.authservice.config.jwt.JwtService;
import com.azue.authservice.domain.entity.RefreshToken;
import com.azue.authservice.domain.entity.User;
import com.azue.authservice.dto.request.LoginRequest;
import com.azue.authservice.dto.request.LogoutRequest;
import com.azue.authservice.dto.request.RefreshTokenRequest;
import com.azue.authservice.dto.request.RegisterRequest;
import com.azue.authservice.dto.response.AuthResponse;
import com.azue.authservice.dto.response.RegisterResponse;
import com.azue.authservice.exception.custom.DuplicateResourceException;
import com.azue.authservice.exception.custom.InvalidTokenException;
import com.azue.authservice.mapper.AuthMapper;
import com.azue.authservice.repository.AuthRepository;
import com.azue.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthMapper authMapper;
    private final AuthRepository authRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional()
    public RegisterResponse register(RegisterRequest request) {
        if (authRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }
        User user = User.create(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        return authMapper.toRegisterResponse(authRepository.save(user));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        User user = authRepository.findByEmail(request.email()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String access = jwtService.generateAccessToken(userDetails);
        String refresh = jwtService.generateRefreshToken(userDetails);

        // Persistir el Refresh Token
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setToken(refresh);
        refreshTokenEntity.setRevoked(false);
        refreshTokenEntity.setExpiresAt(jwtService.getRefreshTokenExpiryDate());
        refreshTokenRepository.save(refreshTokenEntity);
        return new AuthResponse(access, refresh);
    }


    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String incomingToken = request.refreshToken();

        if (!jwtService.isRefreshTokenValid(incomingToken)) {
            throw new InvalidTokenException("Invalid refresh token type or format");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(incomingToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found in database"));

        if (storedToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new InvalidTokenException("Refresh token has expired");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractUsername(incomingToken));

        String newAccess = jwtService.generateAccessToken(userDetails);
        String newRefresh = jwtService.generateRefreshToken(userDetails);

        RefreshToken newTokenEntity = new RefreshToken();
        newTokenEntity.setUser(storedToken.getUser());
        newTokenEntity.setToken(newRefresh);
        newTokenEntity.setRevoked(false);
        newTokenEntity.setExpiresAt(jwtService.getRefreshTokenExpiryDate());

        refreshTokenRepository.save(newTokenEntity);
        return new AuthResponse(newAccess, newRefresh);
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {
        String token = request.refreshToken();
        refreshTokenRepository.findByToken(token).ifPresent(storedToken -> {
            if (!storedToken.isRevoked()) {
                storedToken.setRevoked(true);
                refreshTokenRepository.save(storedToken);
            }
        });
        // TODO: validate if the refresh token should be physically deleted
        // refreshTokenRepository.deleteByToken(token);
    }
}
