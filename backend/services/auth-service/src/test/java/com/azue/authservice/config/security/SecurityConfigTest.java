package com.azue.authservice.config.security;
import com.azue.authservice.config.jwt.JwtAuthenticationFilter;
import com.azue.authservice.config.jwt.JwtService;
import com.azue.authservice.config.properties.AuthSecurityProperties;
import com.azue.authservice.config.userDetails.UserDetailsServiceImpl;
import com.azue.authservice.exception.handler.AuthEntryPointJwt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class SecurityConfigTest {
    private AuthSecurityProperties authSecurityProperties;
    private SecurityConfig securityConfig;
    @BeforeEach
    void setUp() {
        authSecurityProperties = mock(AuthSecurityProperties.class);
        when(authSecurityProperties.getPasswordStrength()).thenReturn(12);
        when(authSecurityProperties.getClientOrigin()).thenReturn("http://localhost:4200, http://localhost:3000");
        AuthEntryPointJwt authEntryPointJwt = mock(AuthEntryPointJwt.class);
        HandlerExceptionResolver handlerExceptionResolver = mock(HandlerExceptionResolver.class);
        securityConfig = new SecurityConfig(authEntryPointJwt, authSecurityProperties, handlerExceptionResolver);
    }
    @Test
    void shouldCreatePasswordEncoderAndProvider() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        DaoAuthenticationProvider provider = securityConfig.authenticationProvider(mock(UserDetailsServiceImpl.class), passwordEncoder);
        assertNotNull(passwordEncoder);
        assertNotNull(provider);
        assertEquals(12, extractStrength(passwordEncoder));
    }
    @Test
    void shouldCreateCorsConfigurationSource() {
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration configuration = source.getCorsConfiguration(new org.springframework.mock.web.MockHttpServletRequest());
        assertNotNull(configuration);
        assertEquals(2, configuration.getAllowedOrigins().size());
        assertEquals("http://localhost:4200", configuration.getAllowedOrigins().get(0));
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
    }
    @Test
    void shouldCreateJwtAuthenticationFilterAndAuthenticationManager() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);
        AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(expectedManager);
        JwtAuthenticationFilter filter = securityConfig.jwtAuthenticationFilter(jwtService, userDetailsService);
        AuthenticationManager actualManager = securityConfig.authenticationManager(authenticationConfiguration);
        assertNotNull(filter);
        assertEquals(expectedManager, actualManager);
    }
    private int extractStrength(PasswordEncoder passwordEncoder) {
        try {
            Field field = passwordEncoder.getClass().getDeclaredField("strength");
            field.setAccessible(true);
            return field.getInt(passwordEncoder);
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}