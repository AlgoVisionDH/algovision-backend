package com.algovision.algovisionbackend.modules.email.service;

import com.algovision.algovisionbackend.modules.email.exception.InvalidVerificationCodeException;
import com.algovision.algovisionbackend.modules.email.exception.MailSendFailedException;
import com.algovision.algovisionbackend.modules.email.exception.TooManyEmailRequestsException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Random random = new Random();

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final EmailRateLimiterService rateLimiterService;

    public void sendVerificationCode(String email) {
        if (!rateLimiterService.canSend(email)) {
            throw new TooManyEmailRequestsException();
        }

        String code = generateCode();
        sendMail(email, code);
        redisTemplate.opsForValue().set(codeKey(email), code, 3, TimeUnit.MINUTES);
    }

    public void verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(codeKey(email));
        if(savedCode == null || !savedCode.equals(code)){
            throw new InvalidVerificationCodeException();
        }
    }

    public void markEmailAsVerified(String email) {
        redisTemplate.opsForValue().set(verifiedKey(email), "true", 10, TimeUnit.MINUTES);
    }

    public boolean isEmailVerified(String email) {
        return "true".equals(redisTemplate.opsForValue().get(verifiedKey(email)));
    }

    private String codeKey(String email) {
        String prefix = "email:";
        return prefix + email;
    }

    private String verifiedKey(String email) {
        String prefix = "email:verified:";
        return prefix + email;
    }

    private String generateCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    private void sendMail(String email, String code) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email);
            helper.setSubject("[Algovision] 이메일 인증 안내");

            String htmlContent = """
                <html>
                <body style="font-family: 'Segoe UI', sans-serif; background-color: #f7f8fa; padding: 20px;">
                    <table align="center" width="480" style="background-color: white; border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                        <tr>
                            <td style="padding: 30px; text-align: center;">
                                <h2 style="color: #2e5aac; margin-bottom: 20px;">Algovision 이메일 인증</h2>
                                <p style="font-size: 15px; color: #444;">안녕하세요 👋<br/>서비스 이용을 위해 아래 인증 코드를 입력해주세요.</p>
                                <div style="margin: 30px 0;">
                                    <span style="display:inline-block; background-color:#2e5aac; color:white; font-size:22px; 
                                                 font-weight:bold; letter-spacing:4px; padding:12px 24px; border-radius:8px;">
                                        %s
                                    </span>
                                </div>
                                <p style="font-size: 13px; color: #888;">해당 코드는 <b>3분간</b>만 유효합니다.<br/>타인에게 공유하지 마세요.</p>
                                <hr style="border:none; border-top:1px solid #eee; margin:30px 0;">
                                <p style="font-size: 12px; color: #999;">본 메일은 발신전용입니다.<br/>문의: support@algovision.com</p>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(code);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new MailSendFailedException();
        }
    }
}
