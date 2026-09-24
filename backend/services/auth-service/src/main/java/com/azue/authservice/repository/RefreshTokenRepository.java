package com.azue.authservice.repository;

import com.azue.authservice.domain.entity.RefreshToken;
import com.azue.authservice.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID>     {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    void deleteByToken(String token);

    @Modifying
    void deleteByUser(User user);

    // Para la limpieza periódica
    @Modifying
    void deleteByExpiresAtBefore(Instant now);
}
