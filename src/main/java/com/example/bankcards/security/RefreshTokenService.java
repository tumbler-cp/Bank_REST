package com.example.bankcards.security;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.entity.security.RefreshToken;
import com.example.bankcards.entity.security.User;
import com.example.bankcards.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Token not found"));

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    public boolean isTokenValid(String token) {
        return refreshTokenRepository.findByToken(token)
            .map(rt -> 
                    !rt.isRevoked() &&
                    !jwtService.isTokenExpired(token))
            .orElse(false);
    }

    @Transactional
    public void cleanUpRevoked() {
        List<RefreshToken> revoked = refreshTokenRepository.findByRevoked(true);
        refreshTokenRepository.deleteAll(revoked);
    }

    @Transactional
    public void save(String token, User user) {
        refreshTokenRepository.save(
            RefreshToken.builder()
                .token(token)
                .user(user)
            .build());
    }
}
