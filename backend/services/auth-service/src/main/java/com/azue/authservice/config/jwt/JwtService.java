package com.azue.authservice.config.jwt;

import com.azue.authservice.config.properties.AuthSecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AuthSecurityProperties authSecurityProperties;

    /**
     * Generates a signed JWT access token.
     * Includes roles for authorization decisions.
     */
    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails, authSecurityProperties.getAccessTokenExpiration(), "access", true);
    }

    /**
     * Generates a refresh token (no roles) used only to renew session.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, authSecurityProperties.getRefreshTokenExpiration(), "refresh", false);
    }

    private String buildToken(UserDetails userDetails, long expiration, String type, boolean includeRoles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        var builder = Jwts.builder()
                .issuer("azen-auth-service")
                .audience().add("azen-platform").and()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .notBefore(now)
                .expiration(expiry)
                .id(UUID.randomUUID().toString())
                .header().add("typ", "JWT").and()
                .claim("type", type);

        if (includeRoles) {
            builder.claim("roles",
                    userDetails.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList());
        }

        return builder.signWith(getSignInKey()).compact();
    }

    /**
     * Validates token signature, expiration and subject.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = parse(token);
            return claims.getSubject().equals(userDetails.getUsername());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validates refresh token and ensures it is not misused as access token.
     */
    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = parse(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses and validates a signed JWT. Throws if invalid or expired.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(authSecurityProperties.getSecretKey()));
    }

    public Instant getRefreshTokenExpiryDate() {
        return Instant.now().plusMillis(authSecurityProperties.getRefreshTokenExpiration());
    }
}
