package com.algovision.algovisionbackend.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest (
        @NotBlank String currentPassword,
        @NotBlank @Size(min=8,max = 64)String newPassword,
        @NotBlank String newPasswordConfirm
){
    @AssertTrue(message = "새 비밀번호와 확인이 일치하지 않습니다.")
    @JsonIgnore
    public boolean isNewPasswordMatched(){
        return newPassword != null && newPassword.equals(newPasswordConfirm);
    }
}
