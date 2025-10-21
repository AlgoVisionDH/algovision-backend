package com.algovision.algovisionbackend.modules.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


class SignUpRequestValidationTest {
    static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void 비밀번호_불일치() {
        SignUpRequest dto = new SignUpRequest("test@test.com", "password123!", "DIFFERENT!", "testNickname");
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("일치하지"));
    }

    @Test
    void 비밀번호_null_입력() {
        SignUpRequest dto = new SignUpRequest("test@test.com", null, null, "testNickname");
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(dto);

        assertThat(violations).anyMatch(v -> v.getMessage().contains("일치하지"));
    }

    @Test
    void 비밀번호_일치() {
        SignUpRequest dto = new SignUpRequest("test@test.com", "password123!", "password123!", "testNickname");
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }
}
