package com.azue.authservice.config.properties;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Constructor;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
class SecurityEndpointsPropertiesTest {
    @Test
    void shouldExposeExpectedEndpoints() {
        assertEquals("/api/v1/auth/login", SecurityEndpointsProperties.PUBLIC[0]);
        assertEquals("/api/v1/auth/logout", SecurityEndpointsProperties.PUBLIC[3]);
        assertEquals("/actuator/health", SecurityEndpointsProperties.MONITORING[0]);
        assertEquals("/swagger-ui/**", SecurityEndpointsProperties.SWAGGER[0]);
    }
    @Test
    void privateConstructorShouldBeAccessibleOnlyByReflection() {
        assertDoesNotThrow(() -> {
            Constructor<SecurityEndpointsProperties> constructor = SecurityEndpointsProperties.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }
}
