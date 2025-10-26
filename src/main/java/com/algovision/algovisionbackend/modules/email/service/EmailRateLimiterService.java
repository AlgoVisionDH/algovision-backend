package com.algovision.algovisionbackend.modules.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailRateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private static final long COOLDOWN_MINUTES = 1;

    public boolean canSend(String email) {
        String key = String.format("email:cooldown:%s", email);

        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            return false;
        }

        redisTemplate.opsForValue().set(key, "cooldown", COOLDOWN_MINUTES, TimeUnit.MINUTES);
        return true;
    }
}
