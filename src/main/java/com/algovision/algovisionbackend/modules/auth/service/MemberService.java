package com.algovision.algovisionbackend.modules.auth.service;

import com.algovision.algovisionbackend.modules.auth.dto.*;

public interface MemberService {
    MemberResponse signup(SignUpRequest request);
    AuthResponse login(LoginRequest request);
    void withdraw(String accessToken, Long memberId);
    MemberResponse changePassword(Long memberId, ChangePasswordRequest request);
    MemberResponse changeNickname(Long memberId, UpdateNicknameRequest request);
    MemberResponse getMember(Long memberId);
}
