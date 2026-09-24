package com.azue.authservice.domain.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

class RefreshTokenTest {

    @Test
    void shouldSetCreatedAtOnPrePersist() throws Exception {
        RefreshToken token = new RefreshToken();
        Method onCreate = RefreshToken.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);

        onCreate.invoke(token);

        assertNotNull(token.getCreatedAt());
        assertFalse(token.getCreatedAt().isAfter(Instant.now().plusSeconds(1)));
    }
}


