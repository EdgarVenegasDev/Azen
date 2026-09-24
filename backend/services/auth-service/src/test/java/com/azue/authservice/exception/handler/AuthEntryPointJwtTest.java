package com.azue.authservice.exception.handler;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
class AuthEntryPointJwtTest {
    @Test
    void shouldWriteUnauthorizedProblemDetails() throws Exception {
        AuthEntryPointJwt entryPoint = new AuthEntryPointJwt(new ObjectMapper());
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        when(request.getRequestURI()).thenReturn("/api/private");
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }
            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
            @Override
            public void write(int b) {
                output.write(b);
            }
        });
        entryPoint.commence(request, response, new org.springframework.security.authentication.BadCredentialsException("bad"));
        String body = output.toString(StandardCharsets.UTF_8);
        assertTrue(body.contains("Unauthorized"));
        assertTrue(body.contains("/api/private"));
        verify(response).setStatus(401);
        verify(response).setContentType("application/problem+json");
    }
}