package com.azue.authservice.exception.handler;

import com.azue.authservice.exception.custom.AccountStatusException;
import com.azue.authservice.exception.custom.DuplicateResourceException;
import com.azue.authservice.exception.custom.InvalidTokenException;
import com.azue.authservice.exception.custom.ResourceNotFoundException;
import com.azue.authservice.exception.dto.ProblemDetails;
import com.azue.authservice.exception.enums.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================================
    // Domain & Business Exceptions
    // =====================================================================

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ProblemDetails> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("Duplicate resource conflict: {}", ex.getMessage());
        return buildResponse(ErrorCode.DUPLICATE_RESOURCE, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ProblemDetails> handleAccountStatusException(AccountStatusException ex, HttpServletRequest request) {
        log.warn("Account restriction: {}", ex.getMessage());
        return buildResponse(ErrorCode.ACCOUNT_INACTIVE, ex.getMessage(), request);
    }

    // =====================================================================
    // Security & Authentication Exceptions
    // =====================================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetails> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Authentication failed on path {}", request.getRequestURI());
        return buildResponse(ErrorCode.INVALID_CREDENTIALS, ErrorCode.INVALID_CREDENTIALS.getDefaultMessage(), request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ProblemDetails> handleUsernameNotFoundException(UsernameNotFoundException ex, HttpServletRequest request) {
        log.warn("User lookup failed during authentication");
        return buildResponse(ErrorCode.USER_NOT_FOUND, ErrorCode.USER_NOT_FOUND.getDefaultMessage(), request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ProblemDetails> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        log.warn("Disabled user account: {}", ex.getMessage());
        return buildResponse(ErrorCode.ACCOUNT_DISABLED, ErrorCode.ACCOUNT_DISABLED.getDefaultMessage(), request);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ProblemDetails> handleLockedException(LockedException ex, HttpServletRequest request) {
        log.warn("Locked user account: {}", ex.getMessage());
        return buildResponse(ErrorCode.ACCOUNT_LOCKED, ErrorCode.ACCOUNT_LOCKED.getDefaultMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetails> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied on path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.getDefaultMessage(), request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetails> handleGenericAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Generic authentication failure on path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ErrorCode.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getDefaultMessage(), request);
    }

    // =====================================================================
    // JWT & Token Exceptions
    // =====================================================================

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetails> handleInvalidTokenException(InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid token exception: {}", ex.getMessage());
        ErrorCode code = ex.getMessage().contains("revoked") ? ErrorCode.REVOKED_TOKEN : ErrorCode.INVALID_TOKEN;
        return buildResponse(code, ex.getMessage(), request);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ProblemDetails> handleExpiredJwtException(ExpiredJwtException ex, HttpServletRequest request) {
        log.warn("Expired JWT token: {}", ex.getMessage());
        return buildResponse(ErrorCode.EXPIRED_TOKEN, ErrorCode.EXPIRED_TOKEN.getDefaultMessage(), request);
    }

    @ExceptionHandler({SignatureException.class, MalformedJwtException.class})
    public ResponseEntity<ProblemDetails> handleInvalidJwtSignatureException(JwtException ex, HttpServletRequest request) {
        log.warn("Malformed or forged JWT token signature: {}", ex.getMessage());
        return buildResponse(ErrorCode.MALFORMED_TOKEN, ErrorCode.MALFORMED_TOKEN.getDefaultMessage(), request);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ProblemDetails> handleGenericJwtException(JwtException ex, HttpServletRequest request) {
        log.warn("JWT processing failure: {}", ex.getMessage());
        return buildResponse(ErrorCode.INVALID_TOKEN, ErrorCode.INVALID_TOKEN.getDefaultMessage(), request);
    }

    // =====================================================================
    // Request Validation & Format Exceptions
    // =====================================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed on path {}", request.getRequestURI());

        StringBuilder errors = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (!errors.isEmpty()) {
                errors.append("; ");
            }
            errors.append(error.getField()).append(": ").append(error.getDefaultMessage());
        }

        return buildResponse(ErrorCode.VALIDATION_FAILED, errors.toString(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetails> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return buildResponse(ErrorCode.CONSTRAINT_VIOLATION, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetails> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON payload received on path {}", request.getRequestURI());
        return buildResponse(ErrorCode.MALFORMED_JSON, ErrorCode.MALFORMED_JSON.getDefaultMessage(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetails> handleMissingParameterException(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String detail = String.format("Required query parameter '%s' of type '%s' is missing.", ex.getParameterName(), ex.getParameterType());
        return buildResponse(ErrorCode.MISSING_PARAMETER, detail, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetails> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String detail = String.format("Parameter '%s' should be of type '%s'.", ex.getName(), requiredType);
        return buildResponse(ErrorCode.TYPE_MISMATCH, detail, request);
    }

    // =====================================================================
    // HTTP Routing & Method Exceptions
    // =====================================================================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetails> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String detail = String.format("HTTP method '%s' is not supported for this endpoint.", ex.getMethod());
        return buildResponse(ErrorCode.METHOD_NOT_ALLOWED, detail, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetails> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        String detail = String.format("Content-Type '%s' is not supported.", ex.getContentType());
        return buildResponse(ErrorCode.UNSUPPORTED_MEDIA_TYPE, detail, request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetails> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        String detail = String.format("No route or static resource found for URI '%s'.", request.getRequestURI());
        return buildResponse(ErrorCode.ENDPOINT_NOT_FOUND, detail, request);
    }

    // =====================================================================
    // Database & Integrity Exceptions
    // =====================================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetails> handleDataIntegrityViolationException(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Database constraint violation on path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(ErrorCode.DATABASE_ERROR, ErrorCode.DATABASE_ERROR.getDefaultMessage(), request);
    }

    // =====================================================================
    // Fallback Unhandled Exceptions (500)
    // =====================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleUnhandledException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled internal server error occurred on path: {}", request.getRequestURI(), ex);
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(), request);
    }

    // =====================================================================
    // Helper Factory Methods
    // =====================================================================

    private ResponseEntity<ProblemDetails> buildResponse(ErrorCode errorCode, String detail, HttpServletRequest request) {
        HttpStatus status = errorCode.getHttpStatus();

        ProblemDetails body = new ProblemDetails(
                "https://api.azen.com/errors/" + errorCode.name().toLowerCase().replace('_', '-'),
                errorCode.name(),
                status.value(),
                detail,
                request.getRequestURI(),
                Instant.now(),
                errorCode
        );

        return ResponseEntity.status(status).body(body);
    }
}
