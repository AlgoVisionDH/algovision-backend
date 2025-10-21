package com.algovision.algovisionbackend.modules.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class ChangePasswordRequestValidationTest {

    static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void 비밀번호_불일치() {
        ChangePasswordRequest dto = new ChangePasswordRequest("currentPw123!", "newPw123!", "DIFFERENT!");
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("일치하지"));
    }

    @Test
    void 비밀번호_null_입력() {
        ChangePasswordRequest dto = new ChangePasswordRequest("currentPw123!", null, null);
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("일치하지"));
    }

    @Test
    void 비밀번호_일치() {
        ChangePasswordRequest dto = new ChangePasswordRequest("currentPw123!", "newPw123!", "newPw123!");
        Set<ConstraintViolation<ChangePasswordRequest>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}