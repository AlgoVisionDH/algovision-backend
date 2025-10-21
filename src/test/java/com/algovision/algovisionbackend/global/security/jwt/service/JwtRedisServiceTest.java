package com.algovision.algovisionbackend.global.security.jwt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtRedisServiceTest {
    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtRedisService jwtRedisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("refreshToken 저장")
    void saveRefreshToken_shouldSaveToRedis() {
        jwtRedisService.saveRefreshToken(1L, "refresh-token", 60000);

        verify(valueOperations).set(eq("jwt:refresh:1"), eq("refresh-token"), eq(60000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("blacklistToken 저장")
    void blacklistToken_shouldSaveToRedis() {
        jwtRedisService.blacklistAccessToken("access-token", 60000);

        verify(valueOperations).set(eq("jwt:blacklist:access-token"), eq("true"), eq(60000L), eq(TimeUnit.MILLISECONDS));
    }
}