package com.azue.authservice.config.jwt;

import com.azue.authservice.config.properties.AuthSecurityProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String SECRET_KEY = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8)
    );

    @Mock
    private AuthSecurityProperties authSecurityProperties;

    @InjectMocks
    private JwtService jwtService;

    @Test
    void generateAccessTokenShouldEncodeAccessClaims() {
        UserDetails userDetails = userDetails();
        givenJwtSecret();
        givenAccessTokenExpiration();

        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        assertEquals("access", jwtService.parse(token).get("type", String.class));
        assertEquals(userDetails.getUsername(), jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertFalse(jwtService.isRefreshTokenValid(token));
    }

    @Test
    void generateRefreshTokenShouldEncodeRefreshClaims() {
        UserDetails userDetails = userDetails();
        givenJwtSecret();
        givenRefreshTokenExpiration();

        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertEquals("refresh", jwtService.parse(token).get("type", String.class));
        assertEquals(userDetails.getUsername(), jwtService.extractUsername(token));
        assertTrue(jwtService.isRefreshTokenValid(token));
    }

    @Test
    void invalidTokenShouldNotValidate() {
        givenJwtSecret();

        assertFalse(jwtService.isTokenValid("not-a-token", userDetails()));
        assertFalse(jwtService.isRefreshTokenValid("not-a-token"));
    }

    private void givenJwtSecret() {
        when(authSecurityProperties.getSecretKey()).thenReturn(SECRET_KEY);
    }

    private void givenAccessTokenExpiration() {
        when(authSecurityProperties.getAccessTokenExpiration()).thenReturn(3_600_000L);
    }

    private void givenRefreshTokenExpiration() {
        when(authSecurityProperties.getRefreshTokenExpiration()).thenReturn(7_200_000L);
    }

    private UserDetails userDetails() {
        return new org.springframework.security.core.userdetails.User(
                "jane.doe@example.com",
                "encoded-password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}

