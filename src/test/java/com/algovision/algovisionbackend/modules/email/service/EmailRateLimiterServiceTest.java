package com.algovision.algovisionbackend.modules.email.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailRateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @InjectMocks
    private EmailRateLimiterService emailRateLimiterService;

    @Test
    void canSend_hasNotKey() {
        String email = "test@test.com";
        when(redisTemplate.hasKey(anyString())).thenReturn(Boolean.FALSE);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        assertTrue(emailRateLimiterService.canSend(email));
    }

    @Test
    void canSend_hasKey() {
        String email = "test@test.com";
        when(redisTemplate.hasKey(anyString())).thenReturn(Boolean.TRUE);

        assertFalse(emailRateLimiterService.canSend(email));
    }
}
