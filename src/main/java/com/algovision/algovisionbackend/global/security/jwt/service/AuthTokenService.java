package com.algovision.algovisionbackend.global.security.jwt.service;

import com.algovision.algovisionbackend.global.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    private final JwtRedisService jwtRedisService;
    private final JwtProvider jwtProvider;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logout(String accessToken, Long memberId){
        jwtRedisService.deleteRefreshToken(memberId);
        long expirationMillis = jwtProvider.getRemainingExpiration(accessToken);
        jwtRedisService.blacklistAccessToken(accessToken, expirationMillis);
    }
}
