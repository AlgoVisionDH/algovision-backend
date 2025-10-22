package com.algovision.algovisionbackend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class MockRedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        when(factory.getConnection()).thenReturn(mock(RedisConnection.class));
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}