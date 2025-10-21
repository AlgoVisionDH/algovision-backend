package com.algovision.algovisionbackend.modules.auth.dto;

import com.algovision.algovisionbackend.global.validation.Nickname;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

public record SignUpRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank String passwordConfirm,
        @NotBlank @Nickname(min = 2, max = 20)
        String nickname
) {
    @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
    @JsonIgnore
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }
}
