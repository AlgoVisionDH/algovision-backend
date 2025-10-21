package com.algovision.algovisionbackend.modules.auth.dto;

import com.algovision.algovisionbackend.global.validation.Nickname;
import jakarta.validation.constraints.NotBlank;

public record UpdateNicknameRequest (
        @NotBlank @Nickname(min=2,max = 20)
        String nickname
){
}
