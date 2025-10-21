package com.algovision.algovisionbackend.modules.auth.controller;

import com.algovision.algovisionbackend.global.security.jwt.service.AuthTokenService;
import com.algovision.algovisionbackend.modules.auth.dto.*;
import com.algovision.algovisionbackend.modules.auth.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;
    private final AuthTokenService authTokenService;

    @PostMapping("/signup")
    public ResponseEntity<MemberResponse> signup(@RequestBody @Valid SignUpRequest request) {
        return ResponseEntity.ok(memberService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(memberService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(memberService.getMember(memberId));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String accessToken,
            @AuthenticationPrincipal Long memberId
    ) {
        String token = extractToken(accessToken);
        authTokenService.logout(token, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @RequestHeader("Authorization") String accessToken,
            @AuthenticationPrincipal Long memberId
    ) {
        String token = extractToken(accessToken);
        memberService.withdraw(token, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<MemberResponse> changePassword(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        MemberResponse response = memberService.changePassword(memberId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-nickname")
    public ResponseEntity<MemberResponse> changeNickname(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid UpdateNicknameRequest request
    ) {
        MemberResponse response = memberService.changeNickname(memberId, request);
        return ResponseEntity.ok(response);
    }

    private String extractToken(String header) {
        return header != null && header.startsWith("Bearer ")
                ? header.substring(7)
                : header;
    }
}
