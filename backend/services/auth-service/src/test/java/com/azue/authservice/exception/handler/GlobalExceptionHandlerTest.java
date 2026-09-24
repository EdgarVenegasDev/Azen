package com.azue.authservice.exception.handler;
import com.azue.authservice.exception.custom.AccountStatusException;
import com.azue.authservice.exception.custom.DuplicateResourceException;
import com.azue.authservice.exception.custom.InvalidTokenException;
import com.azue.authservice.exception.custom.ResourceNotFoundException;
import com.azue.authservice.exception.enums.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;
    private HttpServletRequest request;
    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }
    @Test
    void shouldMapBusinessAndSecurityErrors() {
        assertEquals(HttpStatus.CONFLICT,
                handler.handleDuplicateResourceException(new DuplicateResourceException("dup"), request).getStatusCode());
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND,
                Objects.requireNonNull(handler.handleResourceNotFoundException(new ResourceNotFoundException("missing"), request).getBody()).code());
        assertEquals(ErrorCode.ACCOUNT_INACTIVE,
                Objects.requireNonNull(handler.handleAccountStatusException(new AccountStatusException("inactive"), request).getBody()).code());
        assertEquals(ErrorCode.INVALID_CREDENTIALS,
                Objects.requireNonNull(handler.handleBadCredentialsException(new BadCredentialsException("bad"), request).getBody()).code());
        assertEquals(ErrorCode.USER_NOT_FOUND,
                Objects.requireNonNull(handler.handleUsernameNotFoundException(new UsernameNotFoundException("missing"), request).getBody()).code());
        assertEquals(ErrorCode.ACCOUNT_DISABLED,
                Objects.requireNonNull(handler.handleDisabledException(new DisabledException("disabled"), request).getBody()).code());
        assertEquals(ErrorCode.ACCOUNT_LOCKED,
                Objects.requireNonNull(handler.handleLockedException(new LockedException("locked"), request).getBody()).code());
        assertEquals(ErrorCode.ACCESS_DENIED,
                Objects.requireNonNull(handler.handleAccessDeniedException(new AccessDeniedException("denied"), request).getBody()).code());
        assertEquals(ErrorCode.UNAUTHORIZED,
                Objects.requireNonNull(handler.handleGenericAuthenticationException(new AuthenticationException("auth") {}, request).getBody()).code());
    }
    @Test
    void shouldMapTokenErrors() {
        assertEquals(ErrorCode.REVOKED_TOKEN,
                Objects.requireNonNull(handler.handleInvalidTokenException(new InvalidTokenException("token has been revoked"), request).getBody()).code());
        assertEquals(ErrorCode.INVALID_TOKEN,
                Objects.requireNonNull(handler.handleInvalidTokenException(new InvalidTokenException("token invalid"), request).getBody()).code());
        assertEquals(ErrorCode.EXPIRED_TOKEN,
                Objects.requireNonNull(handler.handleExpiredJwtException(new ExpiredJwtException(null, null, "expired"), request).getBody()).code());
        assertEquals(ErrorCode.MALFORMED_TOKEN,
                Objects.requireNonNull(handler.handleInvalidJwtSignatureException(new SignatureException("sig"), request).getBody()).code());
        assertEquals(ErrorCode.MALFORMED_TOKEN,
                Objects.requireNonNull(handler.handleInvalidJwtSignatureException(new MalformedJwtException("malformed"), request).getBody()).code());
        assertEquals(ErrorCode.INVALID_TOKEN,
                Objects.requireNonNull(handler.handleGenericJwtException(new JwtException("jwt") {}, request).getBody()).code());
    }
    @Test
    void shouldMapRequestAndDatabaseErrors() throws Exception {
        assertEquals(ErrorCode.CONSTRAINT_VIOLATION,
                Objects.requireNonNull(handler.handleConstraintViolationException(new jakarta.validation.ConstraintViolationException("violation", java.util.Set.of()), request).getBody()).code());
        assertEquals(ErrorCode.MALFORMED_JSON,
                Objects.requireNonNull(handler.handleHttpMessageNotReadableException(new org.springframework.http.converter.HttpMessageNotReadableException("bad", (org.springframework.http.HttpInputMessage) null), request).getBody()).code());
        assertEquals(ErrorCode.MISSING_PARAMETER,
                Objects.requireNonNull(handler.handleMissingParameterException(new org.springframework.web.bind.MissingServletRequestParameterException("page", "int"), request).getBody()).code());
        assertEquals(ErrorCode.TYPE_MISMATCH,
                Objects.requireNonNull(handler.handleTypeMismatchException(new org.springframework.web.method.annotation.MethodArgumentTypeMismatchException("abc", Integer.class, "page", null, null), request).getBody()).code());
        assertEquals(ErrorCode.METHOD_NOT_ALLOWED,
                Objects.requireNonNull(handler.handleMethodNotSupportedException(new org.springframework.web.HttpRequestMethodNotSupportedException("PATCH"), request).getBody()).code());
        assertEquals(ErrorCode.UNSUPPORTED_MEDIA_TYPE,
                Objects.requireNonNull(handler.handleMediaTypeNotSupportedException(new org.springframework.web.HttpMediaTypeNotSupportedException("application/xml"), request).getBody()).code());
        assertEquals(ErrorCode.DATABASE_ERROR,
                Objects.requireNonNull(handler.handleDataIntegrityViolationException(new DataIntegrityViolationException("db"), request).getBody()).code());
        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR,
                Objects.requireNonNull(handler.handleUnhandledException(new Exception("boom"), request).getBody()).code());
    }
}
