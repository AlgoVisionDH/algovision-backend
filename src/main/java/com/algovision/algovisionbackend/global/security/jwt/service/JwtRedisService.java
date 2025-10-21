package com.algovision.algovisionbackend.global.security.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtRedisService {
    private static final String REFRESH_PREFIX = "jwt:refresh:";
    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(Long memberId, String refreshToken, long expirationMillis) {
        redisTemplate.opsForValue().set(refreshKey(memberId), refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(Long memberId) {
        return redisTemplate.opsForValue().get(refreshKey(memberId));
    }

    public void deleteRefreshToken(Long memberId) {
        redisTemplate.delete(refreshKey(memberId));
    }

    public void blacklistAccessToken(String accessToken, long expirationMillis) {
        redisTemplate.opsForValue().set(blacklistKey(accessToken), "true", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(accessToken)));
    }

    private String refreshKey(Long memberId){
        return REFRESH_PREFIX + memberId;
    }

    private String blacklistKey(String token){
        return BLACKLIST_PREFIX + token;
    }
}
