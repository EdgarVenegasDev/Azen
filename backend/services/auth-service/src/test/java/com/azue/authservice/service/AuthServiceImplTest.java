package com.azue.authservice.service;

import com.azue.authservice.config.jwt.JwtService;
import com.azue.authservice.domain.entity.RefreshToken;
import com.azue.authservice.domain.entity.User;
import com.azue.authservice.domain.enums.Role;
import com.azue.authservice.domain.enums.UserStatus;
import com.azue.authservice.dto.request.LoginRequest;
import com.azue.authservice.dto.request.LogoutRequest;
import com.azue.authservice.dto.request.RefreshTokenRequest;
import com.azue.authservice.dto.request.RegisterRequest;
import com.azue.authservice.dto.response.AuthResponse;
import com.azue.authservice.dto.response.RegisterResponse;
import com.azue.authservice.exception.custom.DuplicateResourceException;
import com.azue.authservice.mapper.AuthMapper;
import com.azue.authservice.repository.AuthRepository;
import com.azue.authservice.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	private static final String EMAIL = "jane.doe@example.com";
	private static final String PASSWORD = "Password1!";
	private static final String ENCODED_PASSWORD = "encoded-password";
	private static final String ACCESS_TOKEN = "access-token";
	private static final String REFRESH_TOKEN = "refresh-token";
	private static final String NEW_ACCESS_TOKEN = "new-access-token";
	private static final String NEW_REFRESH_TOKEN = "new-refresh-token";
	private static final Instant EXPIRY = Instant.parse("2030-01-01T00:00:00Z");

	@Mock
	private AuthMapper authMapper;

	@Mock
	private AuthRepository authRepository;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@InjectMocks
	private AuthServiceImpl authService;

	@Test
	void registerShouldEncodePasswordPersistUserAndReturnMappedResponse() {
		RegisterRequest request = new RegisterRequest("Jane", "Doe", EMAIL, PASSWORD);
		RegisterResponse expected = new RegisterResponse("Jane", "Doe", EMAIL, "USER", "ACTIVE");

		when(authRepository.existsByEmail(EMAIL)).thenReturn(false);
		when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
		when(authRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(authMapper.toRegisterResponse(any(User.class))).thenReturn(expected);

		RegisterResponse response = authService.register(request);

		assertEquals(expected, response);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(authRepository).save(userCaptor.capture());
		User persistedUser = userCaptor.getValue();

		assertEquals("Jane", persistedUser.getFirstName());
		assertEquals("Doe", persistedUser.getLastName());
		assertEquals(EMAIL, persistedUser.getEmail());
		assertEquals(ENCODED_PASSWORD, persistedUser.getPassword());
		assertEquals(Role.USER, persistedUser.getRole());
		assertEquals(UserStatus.ACTIVE, persistedUser.getStatus());
	}

	@Test
	void registerShouldRejectDuplicateEmail() {
		RegisterRequest request = new RegisterRequest("Jane", "Doe", EMAIL, PASSWORD);
		when(authRepository.existsByEmail(EMAIL)).thenReturn(true);

		assertThrows(DuplicateResourceException.class, () -> authService.register(request));

		verify(authRepository, never()).save(any(User.class));
	}

	@Test
	void loginShouldIssueTokensAndPersistRefreshToken() {
		LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
		User user = User.create("Jane", "Doe", EMAIL, ENCODED_PASSWORD);
		UserDetails userDetails = new org.springframework.security.core.userdetails.User(
				EMAIL,
				ENCODED_PASSWORD,
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);

		when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mock(Authentication.class));
		when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
		when(authRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(jwtService.generateAccessToken(userDetails)).thenReturn(ACCESS_TOKEN);
		when(jwtService.generateRefreshToken(userDetails)).thenReturn(REFRESH_TOKEN);
		when(jwtService.getRefreshTokenExpiryDate()).thenReturn(EXPIRY);
		when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AuthResponse response = authService.login(request);

		assertEquals(ACCESS_TOKEN, response.accessToken());
		assertEquals(REFRESH_TOKEN, response.refreshToken());

		ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
		verify(refreshTokenRepository).save(tokenCaptor.capture());
		RefreshToken persistedToken = tokenCaptor.getValue();

		assertEquals(user, persistedToken.getUser());
		assertEquals(REFRESH_TOKEN, persistedToken.getToken());
		assertFalse(persistedToken.isRevoked());
		assertEquals(EXPIRY, persistedToken.getExpiresAt());
	}

	@Test
	void refreshTokenShouldRevokeCurrentTokenAndIssueNewPair() {
		RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
		User user = User.create("Jane", "Doe", EMAIL, ENCODED_PASSWORD);
		RefreshToken storedToken = new RefreshToken();
		storedToken.setUser(user);
		storedToken.setToken(REFRESH_TOKEN);
		storedToken.setExpiresAt(Instant.parse("2030-01-01T00:00:00Z"));
		storedToken.setRevoked(false);
		UserDetails userDetails = new org.springframework.security.core.userdetails.User(
				EMAIL,
				ENCODED_PASSWORD,
				List.of(new SimpleGrantedAuthority("ROLE_USER"))
		);

		when(jwtService.isRefreshTokenValid(REFRESH_TOKEN)).thenReturn(true);
		when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(storedToken));
		when(jwtService.extractUsername(REFRESH_TOKEN)).thenReturn(EMAIL);
		when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
		when(jwtService.generateAccessToken(userDetails)).thenReturn(NEW_ACCESS_TOKEN);
		when(jwtService.generateRefreshToken(userDetails)).thenReturn(NEW_REFRESH_TOKEN);
		when(jwtService.getRefreshTokenExpiryDate()).thenReturn(EXPIRY);
		when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AuthResponse response = authService.refreshToken(request);

		assertEquals(NEW_ACCESS_TOKEN, response.accessToken());
		assertEquals(NEW_REFRESH_TOKEN, response.refreshToken());

		ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
		verify(refreshTokenRepository, times(2)).save(tokenCaptor.capture());
		List<RefreshToken> savedTokens = tokenCaptor.getAllValues();

		assertTrue(savedTokens.get(0).isRevoked());
		assertEquals(REFRESH_TOKEN, savedTokens.get(0).getToken());
		assertEquals(user, savedTokens.get(0).getUser());

		assertFalse(savedTokens.get(1).isRevoked());
		assertEquals(NEW_REFRESH_TOKEN, savedTokens.get(1).getToken());
		assertEquals(user, savedTokens.get(1).getUser());
		assertEquals(EXPIRY, savedTokens.get(1).getExpiresAt());
	}

	@Test
	void logoutShouldRevokeStoredRefreshToken() {
		LogoutRequest request = new LogoutRequest(REFRESH_TOKEN);
		RefreshToken storedToken = new RefreshToken();
		storedToken.setToken(REFRESH_TOKEN);
		storedToken.setRevoked(false);

		when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(storedToken));

		authService.logout(request);

		assertTrue(storedToken.isRevoked());
		verify(refreshTokenRepository).save(storedToken);
	}

	@Test
	void logoutShouldIgnoreUnknownRefreshToken() {
		LogoutRequest request = new LogoutRequest(REFRESH_TOKEN);
		when(refreshTokenRepository.findByToken(REFRESH_TOKEN)).thenReturn(Optional.empty());

		authService.logout(request);

		verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
	}
}

