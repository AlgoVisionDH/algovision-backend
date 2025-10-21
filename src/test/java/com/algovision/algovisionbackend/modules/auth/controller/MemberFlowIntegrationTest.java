package com.algovision.algovisionbackend.modules.auth.controller;

import com.algovision.algovisionbackend.modules.auth.dto.LoginRequest;
import com.algovision.algovisionbackend.modules.auth.dto.SignUpRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberFlowIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 -> 로그인 -> 내정보조회 -> 로그아웃 -> 재로그인실패 전체 플로우")
    void fullMemberFlowTest() throws Exception {
        // 회원가입
        var signup = new SignUpRequest("test@test.com", "password123!", "password123!", "nickname");
        mockMvc.perform(post("/api/members/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isOk());

        // 로그인
        var login = new LoginRequest("test@test.com", "password123!");
        var loginResult = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        // 내 정보 조회
        var meResult = mockMvc.perform(get("/api/members/me")
                        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andReturn();

        assertThat(meResult.getResponse().getContentAsString()).contains("nickname");

        // 로그아웃
        mockMvc.perform(post("/api/members/logout")
                        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken)))
                .andExpect(status().isOk());

        // 재로그인 실패 (블랙리스트 토큰)
        mockMvc.perform(get("/api/members/me")
                        .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken)))
                .andExpect(status().isUnauthorized());
    }
}
