package com.algovision.algovisionbackend.modules.auth.dto;

public record AuthResponse(
        MemberResponse member,
        String accessToken
) {
}
