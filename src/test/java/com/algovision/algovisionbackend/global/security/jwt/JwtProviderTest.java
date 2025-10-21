package com.algovision.algovisionbackend.global.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {
    @InjectMocks
    JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-test-secret-key-test-secret-key");
        props.setAccessExpiration(600000L);
        props.setRefreshExpiration(86400000L);
        ReflectionTestUtils.setField(jwtProvider, "jwtProperties", props);
        jwtProvider.init();
    }

    @Test
    @DisplayName("accessToken 생성")
    void createAccessToken_shouldContainMemberId() {
        String token = jwtProvider.generateAccessToken(1L);

        Long memberId = jwtProvider.getMemberId(token);
        assertThat(memberId).isEqualTo(1L);
    }

    @Test
    @DisplayName("만료된 토큰 검증 시 false 리턴")
    void validateToken_shouldReturnFalse_whenTokenExpired() throws InterruptedException{
        JwtProperties props = new JwtProperties();
        props.setSecret("test-secret-key-test-secret-key-test-secret-key");
        props.setAccessExpiration(1L);
        props.setRefreshExpiration(1L);
        ReflectionTestUtils.setField(jwtProvider, "jwtProperties", props);
        jwtProvider.init();

        String accessToken = jwtProvider.generateAccessToken(1L);
        String refreshToken = jwtProvider.generateRefreshToken();
        Thread.sleep(5);

        boolean result1 = jwtProvider.validateToken(accessToken);
        boolean result2 = jwtProvider.validateToken(refreshToken);

        assertThat(result1).isFalse();
        assertThat(result2).isFalse();
    }

    @Test
    @DisplayName("위조된 토큰 검증 시 false 리턴")
    void validateToken_shouldReturnFalse_whenTokenTampered(){
        String token = jwtProvider.generateAccessToken(1L) + "tempered";

        boolean result = jwtProvider.validateToken(token);

        assertThat(result).isFalse();
    }
}