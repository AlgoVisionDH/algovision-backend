package com.algovision.algovisionbackend.modules.email.contorller;

import com.algovision.algovisionbackend.config.MockRedisConfig;
import com.algovision.algovisionbackend.modules.email.dto.EmailSendRequest;
import com.algovision.algovisionbackend.modules.email.dto.VerifyEmailRequest;
import com.algovision.algovisionbackend.modules.email.exception.InvalidVerificationCodeException;
import com.algovision.algovisionbackend.modules.email.exception.TooManyEmailRequestsException;
import com.algovision.algovisionbackend.modules.email.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-docs")
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
@Import(MockRedisConfig.class)
class EmailControllerRestDocsTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("이메일 인증 코드 전송 성공 - REST Docs 생성")
    void sendVerificationCode_success_generateDocs() throws Exception {
        EmailSendRequest request = new EmailSendRequest("test@test.com");

        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("인증 코드가 이메일로 전송되었습니다."))
                .andDo(print())
                .andDo(document("email-send-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("인증 이메일 주소")
                        ),
                        responseBody()
                ));
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 실패 - TooManyEmailRequestsException 발생 - REST Docs 생성")
    void sendVerificationCode_tooManyRequests_generateDocs() throws Exception {
        EmailSendRequest request = new EmailSendRequest("spam@test.com");

        doThrow(new TooManyEmailRequestsException()).when(emailService).sendVerificationCode(anyString());

        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andDo(print())
                .andDo(document("email-send-fail-too-many-requests",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("요청 이메일 주소 (과도한 요청)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지 (요청이 너무 많습니다.)"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 실패 - 이메일이 null 또는 빈 값일 경우 - REST Docs 생성")
    void sendVerificationCode_blankEmail_generateDocs() throws Exception {
        EmailSendRequest request = new EmailSendRequest(null);

        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("email-send-fail-blank-email",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.NULL)
                                        .description("요청 이메일 주소 (누락 또는 빈 값)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("오류 메시지 (이메일은 필수 입력값입니다.)"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 인증 코드 전송 실패 - 이메일 형식이 올바르지 않은 경우 - REST Docs 생성")
    void sendVerificationCode_invalidEmail_generateDocs() throws Exception {
        EmailSendRequest request = new EmailSendRequest("notEmailFormat");

        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("email-send-fail-invalid-email",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("요청 이메일 주소 (잘못된 형식)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("오류 메시지 (올바른 이메일 형식이 아닙니다.)"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 성공 - REST Docs 생성")
    void verifyCode_success_generateDocs() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("test@test.com", "123456");

        doNothing().when(emailService).verifyCode(request.email(), request.code());

        mockMvc.perform(post("/api/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("email-verify-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 주소"),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("이메일 인증 코드")
                        ),
                        responseBody()
                ));

        verify(emailService).markEmailAsVerified(request.email());
    }

    @Test
    @DisplayName("이메일 인증 코드 검증 실패 - 코드 불일치 또는 만료 - REST Docs 생성")
    void verifyCode_invalid_generateDocs() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("test@test.com", "999999");

        doThrow(new InvalidVerificationCodeException()).when(emailService).verifyCode(request.email(), request.code());

        mockMvc.perform(post("/api/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andDo(document("email-verify-fail-invalid-code",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("이메일 주소"),
                                fieldWithPath("code").type(JsonFieldType.STRING).description("잘못된 인증 코드")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER).description("HTTP 상태 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("오류 메시지 (인증 코드 불일치/만료)"),
                                fieldWithPath("traceId").type(JsonFieldType.STRING).description("요청 추적용 Trace ID")
                        )
                ));

        verify(emailService, never()).markEmailAsVerified(request.email());
    }
}