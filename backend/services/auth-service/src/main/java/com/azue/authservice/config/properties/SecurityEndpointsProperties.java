package com.azue.authservice.config.properties;

public class SecurityEndpointsProperties {
    private SecurityEndpointsProperties() {
    }

    public static final String[] PUBLIC = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/logout"
    };

    public static final String[] MONITORING = {
            "/actuator/health",
            "/actuator/info"
    };

    public static final String[] SWAGGER = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };
}
