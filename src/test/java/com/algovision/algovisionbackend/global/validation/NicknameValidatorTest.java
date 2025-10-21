package com.algovision.algovisionbackend.global.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NicknameValidatorTest {

    static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    record NickDto(@Nickname String nickname) {
    }

    record NickDtoRequired(@NotBlank @Nickname String nickname) {
    }

    record NickDtoWithMsg(@Nickname(message = "닉네임 규칙 위반") String nickname) {
    }

    record NickDtoAllowBlank(@Nickname(allowBlank = true) String nickname) {
    }

    @Nested
    @DisplayName("유효한 케이스")
    class Valid {
        @ParameterizedTest
        @ValueSource(strings = {"abc", "홍길동", "user_01", "name-123", "한글_English-123", "..."})
        void 통과(String nick) {
            var dto = new NickDto(nick);
            var violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        void null은_NotBlank_없으면_통과() {
            var dto = new NickDto(null);
            var violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        void 공백허용옵션_true면_공백_통과() {
            var dto = new NickDtoAllowBlank("     ");
            var violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("무효한 케이스")
    class Invalid {
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  "})
        void 기본값에선_공백_미허용(String nick) {
            var dto = new NickDto(nick);
            var violations = validator.validate(dto);
            assertThat(violations).isNotEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"has space", "bad*char", "emoji\uD83D\uDE0A", "slash/", "back\\slash"})
        void 허용문자_위반(String nick) {
            var dto = new NickDto(nick);
            var violations = validator.validate(dto);
            assertThat(violations).isNotEmpty();
        }

        @Test
        void 최소길이_위반() {
            var dto = new NickDto("a");
            var violations = validator.validate(dto);
            assertThat(violations).isNotEmpty();
        }

        @Test
        void 최대길이_위반() {
            var dto = new NickDto("a".repeat(50));
            var violations = validator.validate(dto);
            assertThat(violations).isNotEmpty();
        }

        @Test
        void NotBlank와_조합시_null_blank_잡힘() {
            var dto = new NickDtoRequired("    ");
            var violations = validator.validate(dto);
            assertThat(violations).isNotEmpty();
        }

        @Test
        void 커스텀_메세지_단정() {
            var dto = new NickDtoWithMsg("bad*");
            var violations = validator.validate(dto);
            assertThat(violations).anySatisfy(v ->
                    assertThat(v.getMessage()).contains("닉네임 규칙 위반")
            );
        }
    }
}
