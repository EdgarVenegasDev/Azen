package com.azue.authservice.service;

import com.azue.authservice.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenCleanupServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenCleanupService tokenCleanupService;

    @Test
    void purgeExpiredTokensShouldDeleteTokensOlderThanNow() {
        tokenCleanupService.purgeExpiredTokens();

        ArgumentCaptor<Instant> instantCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(refreshTokenRepository).deleteByExpiresAtBefore(instantCaptor.capture());

        Instant capturedInstant = instantCaptor.getValue();
        assertNotNull(capturedInstant);
        assertFalse(capturedInstant.isAfter(Instant.now().plusSeconds(1)));
    }
}
