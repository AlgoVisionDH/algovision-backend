package com.algovision.algovisionbackend.modules.auth.dto;

import com.algovision.algovisionbackend.modules.auth.domain.Role;

import java.time.LocalDateTime;

public record MemberResponse(
        Long id,
        String email,
        String nickname,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}