package com.azue.authservice.exception.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // =====================================================================
    // Domain & Business Errors (1000 - 1999)
    // =====================================================================
    DUPLICATE_RESOURCE(1001, "Resource already exists", HttpStatus.CONFLICT),
    RESOURCE_NOT_FOUND(1002, "Resource not found", HttpStatus.NOT_FOUND),
    ACCOUNT_INACTIVE(1003, "Account is inactive or restricted", HttpStatus.FORBIDDEN),

    // =====================================================================
    // Security & Authentication Errors (2000 - 2999)
    // =====================================================================
    INVALID_CREDENTIALS(2001, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(2002, "User account not found", HttpStatus.UNAUTHORIZED),
    ACCOUNT_DISABLED(2003, "User account is disabled", HttpStatus.FORBIDDEN),
    ACCOUNT_LOCKED(2004, "User account is locked", HttpStatus.FORBIDDEN),
    ACCESS_DENIED(2005, "Access to resource is denied", HttpStatus.FORBIDDEN),
    UNAUTHORIZED(2006, "Authentication required", HttpStatus.UNAUTHORIZED),

    // =====================================================================
    // JWT & Token Errors (3000 - 3999)
    // =====================================================================
    INVALID_TOKEN(3001, "Token is invalid or corrupted", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN(3002, "Token has expired", HttpStatus.UNAUTHORIZED),
    REVOKED_TOKEN(3003, "Token has been revoked", HttpStatus.UNAUTHORIZED),
    MALFORMED_TOKEN(3004, "Token format or signature is invalid", HttpStatus.UNAUTHORIZED),

    // =====================================================================
    // Request Validation & Format Errors (4000 - 4999)
    // =====================================================================
    VALIDATION_FAILED(4001, "Request field validation failed", HttpStatus.BAD_REQUEST),
    CONSTRAINT_VIOLATION(4002, "Database or domain constraint violation", HttpStatus.BAD_REQUEST),
    MALFORMED_JSON(4003, "Request body is unreadable or malformed JSON", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(4004, "Required request parameter is missing", HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH(4005, "Request parameter type mismatch", HttpStatus.BAD_REQUEST),

    // =====================================================================
    // Routing & HTTP Errors (5000 - 5999)
    // =====================================================================
    METHOD_NOT_ALLOWED(5001, "HTTP method not allowed for this route", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(5002, "Unsupported content-type media type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    ENDPOINT_NOT_FOUND(5003, "Requested API endpoint not found", HttpStatus.NOT_FOUND),

    // =====================================================================
    // System & Database Errors (9000 - 9999)
    // =====================================================================
    DATABASE_ERROR(9001, "Database integrity constraint violated", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR(9999, "An unexpected internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
