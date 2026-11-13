package com.horizonbank.homebanking.identity.application;

import com.horizonbank.homebanking.identity.domain.UserStatus;
import com.horizonbank.homebanking.identity.infrastructure.persistence.RefreshTokenEntity;
import com.horizonbank.homebanking.identity.infrastructure.persistence.RefreshTokenJpaRepository;
import com.horizonbank.homebanking.identity.infrastructure.persistence.RoleEntity;
import com.horizonbank.homebanking.identity.infrastructure.persistence.UserEntity;
import com.horizonbank.homebanking.identity.infrastructure.persistence.UserJpaRepository;
import com.horizonbank.homebanking.identity.infrastructure.security.JwtProperties;
import com.horizonbank.homebanking.identity.infrastructure.security.JwtService;
import com.horizonbank.homebanking.identity.infrastructure.security.RefreshTokenCodec;
import com.horizonbank.homebanking.shared.audit.AuditLogger;
import com.horizonbank.homebanking.shared.error.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS = "Invalid credentials";
    private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";

    private final UserJpaRepository users;
    private final RefreshTokenJpaRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenCodec refreshTokenCodec;
    private final JwtProperties jwtProperties;
    private final AuditLogger audit;

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public TokenPair login(String email, String password) {
        Optional<UserEntity> found = users.findByEmailIgnoreCase(email.trim());
        UserEntity user = found
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .orElse(null);

        if (user == null) {
            audit.log(found.map(UserEntity::getId).orElse(null), "LOGIN_FAILED", "USER",
                    found.map(candidate -> candidate.getId().toString()).orElse(null), Map.of());
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        audit.log(user.getId(), "LOGIN_SUCCEEDED", "USER", user.getId().toString(), Map.of());
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public TokenPair refresh(String rawRefreshToken) {
        Instant now = Instant.now();

        RefreshTokenEntity stored = refreshTokens
                .findByTokenHash(refreshTokenCodec.hash(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException(INVALID_REFRESH_TOKEN));

        if (stored.isRevoked()) {
            refreshTokens.revokeAllByUserId(stored.getUser().getId(), now);
            audit.log(stored.getUser().getId(), "REFRESH_TOKEN_REUSE_DETECTED", "USER",
                    stored.getUser().getId().toString(), Map.of());
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }
        if (stored.isExpired(now) || stored.getUser().getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(INVALID_REFRESH_TOKEN);
        }

        stored.revoke(now);
        return issueTokens(stored.getUser());
    }

    private TokenPair issueTokens(UserEntity user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(RoleEntity::getName).sorted().toList();

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getCustomer().getId(), roles);
        String refreshToken = refreshTokenCodec.generate();

        refreshTokens.save(new RefreshTokenEntity(
                user,
                refreshTokenCodec.hash(refreshToken),
                now.plus(jwtProperties.refreshTokenTtl())));

        return new TokenPair(accessToken, refreshToken, jwtProperties.accessTokenTtl().toSeconds());
    }
}