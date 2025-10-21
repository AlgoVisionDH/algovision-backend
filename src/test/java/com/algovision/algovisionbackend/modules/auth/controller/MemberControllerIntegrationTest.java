package com.algovision.algovisionbackend.modules.auth.controller;

import com.algovision.algovisionbackend.modules.auth.domain.Member;
import com.algovision.algovisionbackend.modules.auth.dto.ChangePasswordRequest;
import com.algovision.algovisionbackend.modules.auth.dto.LoginRequest;
import com.algovision.algovisionbackend.modules.auth.dto.SignUpRequest;
import com.algovision.algovisionbackend.modules.auth.dto.UpdateNicknameRequest;
import com.algovision.algovisionbackend.modules.auth.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@Transactional
class MemberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    String accessToken;

    @BeforeEach
    void setup() throws Exception {
        memberRepository.save(Member.builder()
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash(passwordEncoder.encode("password123!"))
                .build());

        LoginRequest loginRequest = new LoginRequest("test@test.com", "password123!");

        MvcResult result = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseJson);
        accessToken = root.path("accessToken").asText();
    }

    @Test
    @DisplayName("회원가입 성공 - REST Docs 생성")
    void signup_success_generateDocs() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test2@test.com",
                "password123!",
                "password123!",
                "nickname2"
        );

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("members-signup-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).description("비밀번호 확인"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("회원 닉네임"),
                                fieldWithPath("role").type(JsonFieldType.STRING).description("회원 권한 (USER / ADMIN)"),
                                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                                fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("수정 일시")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 성공 - REST Docs 생성")
    void login_success_generateDocs() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "password123!");

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("members-login-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("로그인 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                subsectionWithPath("member").description("로그인한 회원 정보"),
                                fieldWithPath("member.id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("member.email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("member.nickname").type(JsonFieldType.STRING).description("회원 닉네임"),
                                fieldWithPath("member.role").type(JsonFieldType.STRING).description("회원 권한 (USER / ADMIN)"),
                                fieldWithPath("member.createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                                fieldWithPath("member.updatedAt").type(JsonFieldType.STRING).description("수정 일시"),
                                fieldWithPath("accessToken").type(JsonFieldType.STRING).description("JWT 액세스 토큰")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복 - REST Docs 생성")
    void signup_fail_duplicateEmail_generateDocs() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "password123!",
                "password123!",
                "nickname2"
        );

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print())
                .andDo(document("members-signup-fail-duplicate-email",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일 (중복된 이메일)"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).description("비밀번호 확인"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복 - REST Docs 생성")
    void signup_fail_duplicateNickname_generateDocs() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test2@test.com",
                "password123!",
                "password123!",
                "nickname"
        );

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print())
                .andDo(document("members-signup-fail-duplicate-nickname",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).description("비밀번호 확인"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임 (중복된 닉네임)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호 불일치 - REST Docs 생성")
    void signup_fail_passwordMismatch_generateDocs() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test2@test.com",
                "password123!",
                "differentPassword!",
                "nickname2"
        );

        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("members-signup-fail-password-mismatch",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"),
                                fieldWithPath("passwordConfirm").type(JsonFieldType.STRING).description("비밀번호 확인 (불일치)"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치 - REST Docs 생성")
    void login_fail_wrongPassword_generateDocs() throws Exception {
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword!");

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andDo(document("members-login-fail-wrong-password",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("로그인 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("잘못된 비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일 - REST Docs 생성")
    void login_fail_notFoundEmail_generateDocs() throws Exception {
        LoginRequest request = new LoginRequest("notfound@test.com", "password123!");

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andDo(print())
                .andDo(document("members-login-fail-notfound-email",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("존재하지 않는 회원 이메일"),
                                fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("내 정보 조회 성공 - REST Docs 생성")
    void getMe_success_generateDocs() throws Exception {
        mockMvc.perform(get("/api/members/me")
                        .header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("members-get-me-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("회원 닉네임"),
                                fieldWithPath("role").type(JsonFieldType.STRING).description("회원 권한 (USER / ADMIN)"),
                                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                                fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("수정 일시")
                        )
                ));
    }

    @Test
    @DisplayName("내 정보 조회 실패 - 잘못된 토큰 - REST Docs 생성")
    void getMe_fail_invalidToken_generateDocs() throws Exception {
        mockMvc.perform(get("/api/members/me")
                        .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isForbidden())
                .andDo(print())
                .andDo(document("members-get-me-fail-invalid-token",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("로그아웃 성공 - REST Docs 생성")
    void logout_success_generateDocs() throws Exception {
        mockMvc.perform(post("/api/members/logout")
                        .header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("members-logout-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 - REST Docs 생성")
    void withdraw_success_generateDocs() throws Exception {
        mockMvc.perform(post("/api/members/withdraw")
                        .header("Authorization", String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("members-withdraw-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint())
                ));
    }

    @Test
    @DisplayName("비밀번호 변경 성공 - REST Docs 생성")
    void changePassword_success_generateDocs() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "password123!", "newPassword123!", "newPassword123!"
        );

        mockMvc.perform(post("/api/members/change-password")
                        .header("Authorization", String.format("Bearer %s", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("members-change-password-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("currentPassword").type(JsonFieldType.STRING).description("현재 비밀번호"),
                                fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새 비밀번호"),
                                fieldWithPath("newPasswordConfirm").type(JsonFieldType.STRING).description("새 비밀번호 확인")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("회원 닉네임"),
                                fieldWithPath("role").type(JsonFieldType.STRING).description("회원 권한 (USER / ADMIN)"),
                                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                                fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("수정 일시")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치 - REST Docs 생성")
    void changePassword_fail_wrongCurrentPassword_generateDocs() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongPassword", "newPassword123!", "newPassword123!"
        );

        mockMvc.perform(post("/api/members/change-password")
                        .header("Authorization", String.format("Bearer %s", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("members-change-password-fail-wrong-current-password",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("currentPassword").type(JsonFieldType.STRING).description("현재 비밀번호 (잘못된 값)"),
                                fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새 비밀번호"),
                                fieldWithPath("newPasswordConfirm").type(JsonFieldType.STRING).description("새 비밀번호 확인")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지 (예: 비밀번호가 일치하지 않습니다.)"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호와 새 비밀번호 일치 - REST Docs 생성")
    void changePassword_fail_currentPassword_equal_newPassword_generateDocs() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "password123!", "password123!", "password123!"
        );

        mockMvc.perform(post("/api/members/change-password")
                        .header("Authorization", String.format("Bearer %s", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print())
                .andDo(document("members-change-password-fail-current-password-equal-new-password",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("currentPassword").type(JsonFieldType.STRING).description("현재 비밀번호 (일치)"),
                                fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새 비밀번호 (일치)"),
                                fieldWithPath("newPasswordConfirm").type(JsonFieldType.STRING).description("새 비밀번호 확인")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지 (예: 비밀번호가 일치하지 않습니다.)"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호와 새 비밀번호 확인 불일치 - REST Docs 생성")
    void changePassword_fail_newPassword_not_equal_newPasswordConfirm_generateDocs() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "password123!", "newPassword123!", "newDifferentPassword123!"
        );

        mockMvc.perform(post("/api/members/change-password")
                        .header("Authorization", String.format("Bearer %s", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("members-change-password-fail-new-password-not-equal-new-password-confirm",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("currentPassword").type(JsonFieldType.STRING).description("현재 비밀번호"),
                                fieldWithPath("newPassword").type(JsonFieldType.STRING).description("새 비밀번호 (불일치)"),
                                fieldWithPath("newPasswordConfirm").type(JsonFieldType.STRING).description("새 비밀번호 확인 (불일치)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지 (예: 비밀번호가 일치하지 않습니다.)"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("닉네임 변경 성공 - REST Docs 생성")
    void changeNickname_success_generateDocs() throws Exception {
        UpdateNicknameRequest request = new UpdateNicknameRequest("newNickname");

        mockMvc.perform(post("/api/members/change-nickname")
                        .header("Authorization", String.format("Bearer %s", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("members-change-nickname-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("변경할 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("변경된 닉네임"),
                                fieldWithPath("role").type(JsonFieldType.STRING).description("회원 권한"),
                                fieldWithPath("createdAt").type(JsonFieldType.STRING).description("생성 일시"),
                                fieldWithPath("updatedAt").type(JsonFieldType.STRING).description("수정 일시")
                        )
                ));
    }

    @Test
    @DisplayName("닉네임 변경 실패 - 중복 닉네임 - REST Docs 생성")
    void changeNickname_fail_duplicate_generateDocs() throws Exception {
        UpdateNicknameRequest request = new UpdateNicknameRequest("nickname");

        mockMvc.perform(post("/api/members/change-nickname")
                        .header("Authorization", String.format("Bearer %s", accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andDo(print())
                .andDo(document("members-change-nickname-fail-duplicate",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("nickname").type(JsonFieldType.STRING).description("중복된 닉네임")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지 (예: 닉네임이 이미 사용 중입니다.)"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }
}