package com.algovision.algovisionbackend.modules.email.controller;

import com.algovision.algovisionbackend.modules.email.dto.EmailSendRequest;
import com.algovision.algovisionbackend.modules.email.dto.VerifyEmailRequest;
import com.algovision.algovisionbackend.modules.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendVerificationCode(@RequestBody @Valid EmailSendRequest request) {
        emailService.sendVerificationCode(request.email());
        return ResponseEntity.ok("인증 코드가 이메일로 전송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestBody @Valid VerifyEmailRequest request) {
        emailService.verifyCode(request.email(), request.code());

        emailService.markEmailAsVerified(request.email());
        return ResponseEntity.ok("이메일 인증 성공!");
    }
}
