package com.azue.authservice.config.properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
class AuthSecurityPropertiesTest {
    @Test
    void shouldBindAndExposeProperties() {
        AuthSecurityProperties properties = new AuthSecurityProperties();
        properties.setSecretKey("01234567890123456789012345678901");
        properties.setAccessTokenExpiration(1000L);
        properties.setRefreshTokenExpiration(2000L);
        properties.setPasswordStrength(12);
        properties.clientOrigin = "http://localhost:4200";
        assertEquals("01234567890123456789012345678901", properties.getSecretKey());
        assertEquals(1000L, properties.getAccessTokenExpiration());
        assertEquals(2000L, properties.getRefreshTokenExpiration());
        assertEquals(12, properties.getPasswordStrength());
        assertEquals("http://localhost:4200", properties.clientOrigin);
    }
}
