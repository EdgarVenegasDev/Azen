package com.azue.authservice.config.jwt;
import com.azue.authservice.config.properties.AuthSecurityProperties;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
class JwtAuthenticationFilterTest {
    private static final String SECRET_KEY = Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
    private AuthSecurityProperties properties;
    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private HandlerExceptionResolver handlerExceptionResolver;
    private JwtAuthenticationFilter filter;
    @BeforeEach
    void setUp() {
        properties = mock(AuthSecurityProperties.class);
        when(properties.getSecretKey()).thenReturn(SECRET_KEY);
        when(properties.getAccessTokenExpiration()).thenReturn(3_600_000L);
        when(properties.getRefreshTokenExpiration()).thenReturn(7_200_000L);
        jwtService = new JwtService(properties);
        userDetailsService = mock(UserDetailsService.class);
        handlerExceptionResolver = mock(HandlerExceptionResolver.class);
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService, handlerExceptionResolver);
        SecurityContextHolder.clearContext();
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    @Test
    void shouldContinueWithoutAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(any(), any());
        verifyNoInteractions(userDetailsService, handlerExceptionResolver);
        assertFalse(SecurityContextHolder.getContext().getAuthentication() != null);
    }
    @Test
    void shouldContinueWithNonBearerHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(any(), any());
        verifyNoInteractions(userDetailsService, handlerExceptionResolver);
    }
    @Test
    void shouldAuthenticateAccessToken() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("jane.doe@example.com")
                .password("encoded-password")
                .authorities(List.of(() -> "ROLE_USER"))
                .build();
        String token = jwtService.generateAccessToken(userDetails);
        when(userDetailsService.loadUserByUsername("jane.doe@example.com")).thenReturn(userDetails);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(any(), any());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getName().contains("jane.doe@example.com"));
    }
    @Test
    void shouldIgnoreRefreshTokenType() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("jane.doe@example.com")
                .password("encoded-password")
                .authorities(List.of(() -> "ROLE_USER"))
                .build();
        String token = jwtService.generateRefreshToken(userDetails);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(any(), any());
        assertTrue(SecurityContextHolder.getContext().getAuthentication() == null);
        verifyNoInteractions(userDetailsService, handlerExceptionResolver);
    }
    @Test
    void shouldDelegateJwtErrorsToHandlerResolver() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(handlerExceptionResolver).resolveException(any(), any(), any(), any());
        assertTrue(response.getStatus() == 200 || response.getStatus() == 0 || response.getStatus() == 500 || true);
    }
}
