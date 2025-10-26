package com.algovision.algovisionbackend.modules.email.service;

import com.algovision.algovisionbackend.modules.email.exception.InvalidVerificationCodeException;
import com.algovision.algovisionbackend.modules.email.exception.MailSendFailedException;
import com.algovision.algovisionbackend.modules.email.exception.TooManyEmailRequestsException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private EmailRateLimiterService rateLimiterService;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private MimeMessage mimeMessage;
    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 성공, 메일과 코드가 저장")
    void sendVerificationCode_success() {
        String email = "test@test.com";
        when(rateLimiterService.canSend(email)).thenReturn(true);

        emailService.sendVerificationCode(email);

        verify(rateLimiterService).canSend(email);
        verify(mailSender).send(any(MimeMessage.class));
        verify(valueOperations).set(startsWith("email:"), anyString(), eq(3L), eq(TimeUnit.MINUTES));
    }


    @Test
    @DisplayName("RateLimiter 제한 초과 시 TooManyEmailRequestException 발생")
    void sendVerificationCode_tooManyRequests() {
        String email = "test@test.com";
        when(rateLimiterService.canSend(email)).thenReturn(false);

        assertThrows(TooManyEmailRequestsException.class, () -> emailService.sendVerificationCode(email));

        verify(mailSender, never()).send(any(MimeMessage.class));
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("저장된 인증 코드와 일치할 경우 검증 성공")
    void verifyCode_valid() {
        String email = "test@test.com";
        String code = "123456";
        when(valueOperations.get("email:" + email)).thenReturn(code);

        assertDoesNotThrow(() -> emailService.verifyCode(email, "123456"));
    }

    @Test
    @DisplayName("저장된 인증 코드와 불일치할 경우 검증 실패")
    void verifyCode_invalid() {
        String email = "test@test.com";
        when(valueOperations.get("email:" + email)).thenReturn("654321");

        assertThrows(InvalidVerificationCodeException.class, () -> emailService.verifyCode(email, "123456"));
    }

    @Test
    @DisplayName("저장된 인증 코드가 없을 경우 검증 실패")
    void verifyCode_noStoredCode() {
        String email = "test@test.com";
        when(valueOperations.get("email:" + email)).thenReturn(null);

        assertThrows(InvalidVerificationCodeException.class, () -> emailService.verifyCode(email, "123456"));
    }

    @Test
    @DisplayName("이메일 인증 완료 시 Redis에 'verified:true' 저장")
    void markEmailAsVerified() {
        String email = "test@test.com";

        emailService.markEmailAsVerified(email);

        verify(valueOperations).set("email:verified:" + email, "true", 10L, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("Redis에 저장된 값이 'true'면 인증 상태 확인 성공")
    void isEmailVerified_true() {
        String email = "test@test.com";
        when(valueOperations.get("email:verified:" + email)).thenReturn("true");

        assertTrue(emailService.isEmailVerified(email));
    }

    @Test
    @DisplayName("Redis에 저장된 값이 'false'면 인증 상태 확인 실패")
    void isEmailVerified_false() {
        String email = "test@test.com";
        when(valueOperations.get("email:verified:" + email)).thenReturn("false");

        assertFalse(emailService.isEmailVerified(email));
    }

    @Test
    @DisplayName("메일 전송 실패 시 MailSendFailedException 발생")
    void sendMail_fails_throwsException() {
        String email = "fail@test.com";
        when(rateLimiterService.canSend(email)).thenReturn(true);
        doThrow(MailSendException.class).when(mailSender).send(any(MimeMessage.class));

        assertThrows(MailSendFailedException.class, () -> emailService.sendVerificationCode(email));
    }
}
