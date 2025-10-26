package com.algovision.algovisionbackend.modules.auth.service.impl;

import com.algovision.algovisionbackend.global.security.jwt.JwtProperties;
import com.algovision.algovisionbackend.global.security.jwt.JwtProvider;
import com.algovision.algovisionbackend.global.security.jwt.service.AuthTokenService;
import com.algovision.algovisionbackend.global.security.jwt.service.JwtRedisService;
import com.algovision.algovisionbackend.modules.auth.domain.Member;
import com.algovision.algovisionbackend.modules.auth.domain.Role;
import com.algovision.algovisionbackend.modules.auth.dto.*;
import com.algovision.algovisionbackend.modules.auth.exception.*;
import com.algovision.algovisionbackend.modules.auth.mapper.MemberMapper;
import com.algovision.algovisionbackend.modules.auth.repository.MemberRepository;
import com.algovision.algovisionbackend.modules.email.exception.EmailNotVerifiedException;
import com.algovision.algovisionbackend.modules.email.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberMapper memberMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtRedisService jwtRedisService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private EmailService emailService;

    private AuthTokenService authTokenService;
    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        authTokenService = new AuthTokenService(jwtRedisService, jwtProvider);
        memberService = new MemberServiceImpl(
                memberRepository,
                memberMapper,
                passwordEncoder,
                jwtRedisService,
                authTokenService,
                jwtProperties,
                jwtProvider,
                emailService
        );
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "password123!",
                "password123!",
                "nickname"
        );

        when(emailService.isEmailVerified(anyString())).thenReturn(true);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        Member saved = Member.builder()
                .email(request.email())
                .nickname(request.nickname())
                .passwordHash(request.password())
                .build();
        when(memberRepository.save(any())).thenReturn(saved);
        when(memberMapper.toResponse(saved)).thenReturn(new MemberResponse(
                1L,
                request.email(),
                request.nickname(),
                Role.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        when(emailService.isEmailVerified(anyString())).thenReturn(true);
        MemberResponse response = memberService.signup(request);

        assertNotNull(response);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 인증되지 않은 이메일")
    void signup_fail_unverifiedEmail() {
        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "password123!",
                "password123!",
                "nickname"
        );

        when(emailService.isEmailVerified(anyString())).thenReturn(false);

        assertThrows(EmailNotVerifiedException.class, () -> memberService.signup(request));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_fail_to_exists_email() {
        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "password123!",
                "password123!",
                "nickname"
        );

        when(emailService.isEmailVerified(anyString())).thenReturn(true);
        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> memberService.signup(request));

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void signup_fail_to_exists_nickname() {
        SignUpRequest request = new SignUpRequest(
                "test@test.com",
                "password123!",
                "password123!",
                "nickname"
        );

        when(emailService.isEmailVerified(anyString())).thenReturn(true);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        assertThrows(DuplicateNicknameException.class, () -> memberService.signup(request));

        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "password123!"
        );

        Member saved = Member.builder()
                .id(1L)
                .email(request.email())
                .nickname("nickname")
                .passwordHash(request.password())
                .build();

        MemberResponse member = new MemberResponse(
                1L,
                request.email(),
                "nickname",
                Role.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(memberRepository.findMemberByEmail(anyString())).thenReturn(Optional.of(saved));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateAccessToken(anyLong())).thenReturn("accessToken");
        when(jwtProvider.generateRefreshToken()).thenReturn("refreshToken");
        when(memberMapper.toResponse(any())).thenReturn(member);
        doNothing().when(jwtRedisService)
                .saveRefreshToken(anyLong(), anyString(), anyLong());

        AuthResponse response = memberService.login(request);

        assertNotNull(response);
        assertNotNull(response.member());
        assertNotNull(response.accessToken());

        verify(jwtRedisService).saveRefreshToken(eq(1L), anyString(), anyLong());
        verify(jwtProvider).generateAccessToken(eq(1L));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 회원")
    void login_fail_to_member_not_found() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "password123!"
        );

        when(memberRepository.findMemberByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidLoginException.class, () -> memberService.login(request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtProvider, never()).generateAccessToken(anyLong());
        verify(jwtProvider, never()).generateRefreshToken();
        verify(jwtRedisService, never()).saveRefreshToken(
                anyLong(),
                anyString(),
                anyLong()
        );
        verify(memberMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_to_password_mismatch() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "password123!"
        );

        Member saved = Member.builder()
                .id(1L)
                .email(request.email())
                .nickname("nickname")
                .passwordHash(request.password())
                .build();

        when(memberRepository.findMemberByEmail(anyString())).thenReturn(Optional.of(saved));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidLoginException.class, () -> memberService.login(request));

        verify(jwtProvider, never()).generateAccessToken(anyLong());
        verify(jwtProvider, never()).generateRefreshToken();
        verify(jwtRedisService, never()).saveRefreshToken(
                anyLong(),
                anyString(),
                anyLong()
        );
        verify(memberMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("로그인 실패 - AccessToken 생성 중 예외 발생")
    void login_fail_to_throw_exception_when_generate_access_token() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "password123!"
        );

        Member saved = Member.builder()
                .id(1L)
                .email(request.email())
                .nickname("nickname")
                .passwordHash(request.password())
                .build();

        when(memberRepository.findMemberByEmail(anyString())).thenReturn(Optional.of(saved));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateAccessToken(anyLong())).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> memberService.login(request));

        verify(jwtProvider, never()).generateRefreshToken();
        verify(jwtRedisService, never()).saveRefreshToken(
                anyLong(),
                anyString(),
                anyLong()
        );
        verify(memberMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("로그인 실패 - RefreshToken 생성 중 예외 발생")
    void login_fail_to_throw_exception_when_generate_refresh_token() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "password123!"
        );

        Member saved = Member.builder()
                .id(1L)
                .email(request.email())
                .nickname("nickname")
                .passwordHash(request.password())
                .build();

        when(memberRepository.findMemberByEmail(anyString())).thenReturn(Optional.of(saved));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateAccessToken(anyLong())).thenReturn("accessToken");
        when(jwtProvider.generateRefreshToken()).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> memberService.login(request));

        verify(jwtRedisService, never()).saveRefreshToken(
                anyLong(),
                anyString(),
                anyLong()
        );
        verify(memberMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("로그인 실패 - RefreshToken 저장 중 예외 발생")
    void login_fail_to_throw_exception_when_save_refresh_token() {
        LoginRequest request = new LoginRequest(
                "test@test.com",
                "password123!"
        );

        Member saved = Member.builder()
                .id(1L)
                .email(request.email())
                .nickname("nickname")
                .passwordHash(request.password())
                .build();

        when(memberRepository.findMemberByEmail(anyString())).thenReturn(Optional.of(saved));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateAccessToken(anyLong())).thenReturn("accessToken");
        when(jwtProvider.generateRefreshToken()).thenReturn("refreshToken");
        doThrow(new RuntimeException())
                .when(jwtRedisService).saveRefreshToken(anyLong(), anyString(), anyLong());

        assertThrows(RuntimeException.class, () -> memberService.login(request));

        verify(memberMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_success() {
        String accessToken = "accessToken";
        long memberId = 1L;

        doNothing().when(jwtRedisService).deleteRefreshToken(anyLong());
        when(jwtProvider.getRemainingExpiration(anyString())).thenReturn(1L);
        doNothing().when(jwtRedisService).blacklistAccessToken(anyString(), anyLong());

        assertDoesNotThrow(() -> authTokenService.logout(accessToken, memberId));
        verify(jwtRedisService, times(1)).deleteRefreshToken(anyLong());
        verify(jwtProvider, times(1)).getRemainingExpiration(anyString());
        verify(jwtRedisService, times(1)).blacklistAccessToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("로그아웃 실패 - RefreshToken 삭제 중 예외 발생")
    void logout_fail_to_throw_exception_when_delete_refresh_token() {
        String accessToken = "accessToken";
        long memberId = 1L;

        doThrow(new RuntimeException()).when(jwtRedisService).deleteRefreshToken(anyLong());

        assertThrows(RuntimeException.class, () -> authTokenService.logout(accessToken, memberId));
        verify(jwtRedisService, times(1)).deleteRefreshToken(anyLong());
    }

    @Test
    @DisplayName("로그아웃 실패 - 남은 만료 시간 가져오는 중 예외 발생")
    void logout_fail_to_throw_exception_when_get_remaining_expiration() {
        String accessToken = "accessToken";
        long memberId = 1L;

        doNothing().when(jwtRedisService).deleteRefreshToken(anyLong());
        doThrow(new RuntimeException()).when(jwtProvider).getRemainingExpiration(anyString());

        assertThrows(RuntimeException.class, () -> authTokenService.logout(accessToken, memberId));
        verify(jwtRedisService, times(1)).deleteRefreshToken(anyLong());
        verify(jwtProvider, times(1)).getRemainingExpiration(anyString());
    }

    @Test
    @DisplayName("로그아웃 실패 - balcklist token으로 로그인 시도")
    void logout_fail_to_throw_exception_when_blacklist_access_token() {
        String accessToken = "accessToken";
        long memberId = 1L;

        doNothing().when(jwtRedisService).deleteRefreshToken(anyLong());
        when(jwtProvider.getRemainingExpiration(anyString())).thenReturn(1L);
        doThrow(new RuntimeException()).when(jwtRedisService).blacklistAccessToken(anyString(), anyLong());

        assertThrows(RuntimeException.class, () -> authTokenService.logout(accessToken, memberId));
        verify(jwtRedisService, times(1)).deleteRefreshToken(anyLong());
        verify(jwtProvider, times(1)).getRemainingExpiration(anyString());
        verify(jwtRedisService, times(1)).blacklistAccessToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void withdraw_success() {
        String accessToken = "accessToken";
        long memberId = 1L;

        Member mockMember = mock(Member.class);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));

        doNothing().when(jwtRedisService).deleteRefreshToken(memberId);
        when(jwtProvider.getRemainingExpiration(anyString())).thenReturn(1L);
        doNothing().when(jwtRedisService).blacklistAccessToken(anyString(), anyLong());

        assertDoesNotThrow(() -> memberService.withdraw(accessToken, memberId));

        verify(jwtRedisService).deleteRefreshToken(memberId);
        verify(jwtRedisService).blacklistAccessToken(eq(accessToken), anyLong());
        verify(mockMember).softDelete();
    }

    @Test
    @DisplayName("회원 탈퇴 실패 - 존재하지 않는 회원")
    void withdraw_fail_member_not_found() {
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(MemberNotFoundException.class,
                () -> memberService.withdraw("accessToken", 99L));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() {
        long memberId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "currentPassword",
                "newPassword",
                "newPassword"
        );

        Member member = Member.builder()
                .id(1L)
                .nickname("nickname")
                .email("test@test.com")
                .passwordHash("newPassword")
                .build();

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(memberMapper.toResponse(any())).thenReturn(new MemberResponse(
                1L,
                "test@test.com",
                "nickname",
                Role.USER,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));

        MemberResponse response = memberService.changePassword(memberId, request);

        assertNotNull(response);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 회원 없음")
    void changePassword_fail_member_not_found() {
        long memberId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "currentPassword", "newPassword", "newPassword"
        );

        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class,
                () -> memberService.changePassword(memberId, request));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_fail_password_mismatch() {
        long memberId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "wrongPassword", "newPassword", "newPassword"
        );

        Member member = Member.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash("encodedPassword")
                .build();

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(PasswordMismatchException.class,
                () -> memberService.changePassword(memberId, request));

        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 새 비밀번호와 새 비밀번호 확인 불일치")
    void changePassword_no_change_when_new_password_mismatch() {
        long memberId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "currentPassword", "newPassword1", "newPassword2" // 불일치
        );

        Member member = spy(Member.builder()
                .id(memberId)
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash("encodedCurrentPassword")
                .build());

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true).thenReturn(false);

        memberService.changePassword(memberId, request);

        verify(member, never()).changePassword(anyString());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 기존 비밀번호와 새 비밀번호가 동일")
    void changePassword_no_change_when_equal_curPassword_and_newPassword() {
        long memberId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest(
                "currentPassword", "currentPassword", "currentPassword"
        );

        Member member = spy(Member.builder()
                .id(memberId)
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash("encodedCurrentPassword")
                .build());

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true).thenReturn(true);

        assertThrows(SamePasswordException.class, () -> memberService.changePassword(memberId, request));

        verify(member, never()).changePassword(anyString());
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void changeNickname_success() {
        long memberId = 1L;
        UpdateNicknameRequest request = new UpdateNicknameRequest("newNickname");

        Member member = spy(Member.builder()
                .id(memberId)
                .email("test@test.com")
                .nickname("oldNickname")
                .passwordHash("password")
                .build());

        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(memberMapper.toResponse(any())).thenReturn(new MemberResponse(
                1L, "test@test.com", "newNickname", Role.USER, LocalDateTime.now(), LocalDateTime.now()
        ));

        MemberResponse response = memberService.changeNickname(memberId, request);

        assertNotNull(response);
        assertEquals("newNickname", response.nickname());
        verify(member).updateNickname("newNickname");
    }

    @Test
    @DisplayName("닉네임 변경 실패 - 중복 닉네임")
    void changeNickname_fail_to_duplicate_nickname() {
        long memberId = 1L;
        UpdateNicknameRequest request = new UpdateNicknameRequest("duplicateNickname");

        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        assertThrows(DuplicateNicknameException.class,
                () -> memberService.changeNickname(memberId, request));

        verify(memberRepository, never()).findById(anyLong());
        verify(memberMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("닉네임 변경 실패 - 회원 없음")
    void changeNickname_fail_to_member_not_found() {
        long memberId = 1L;
        UpdateNicknameRequest request = new UpdateNicknameRequest("newNickname");

        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class,
                () -> memberService.changeNickname(memberId, request));

        verify(memberMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMember_success() {
        long memberId = 1L;
        Member member = Member.builder()
                .id(memberId)
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash("password")
                .build();

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(memberMapper.toResponse(any())).thenReturn(new MemberResponse(
                1L, "test@test.com", "nickname", Role.USER, LocalDateTime.now(), LocalDateTime.now()
        ));

        MemberResponse response = memberService.getMember(memberId);

        assertNotNull(response);
        assertEquals("nickname", response.nickname());
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("회원 정보 조회 실패 - 존재하지 않는 회원")
    void getMember_fail_to_member_not_found() {
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class,
                () -> memberService.getMember(1L));

        verify(memberMapper, never()).toResponse(any());
    }
}