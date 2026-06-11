package com.marcus.eventhub.auth;

import com.marcus.eventhub.common.exception.BusinessException;
import com.marcus.eventhub.user.User;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long expirationMs;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${eventhub.jwt.refresh-expiration-ms}") long expirationMs
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.expirationMs = expirationMs;
    }

    @Transactional
    public String createForUser(User user) {
        String token = UUID.randomUUID().toString();
        refreshTokenRepository.save(new RefreshToken(token, user, Instant.now().plusMillis(expirationMs)));
        return token;
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            throw new BusinessException("Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public String rotate(RefreshToken refreshToken) {
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
        return createForUser(refreshToken.getUser());
    }

    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .ifPresent(existing -> {
                    existing.revoke();
                    refreshTokenRepository.save(existing);
                });
    }
}
