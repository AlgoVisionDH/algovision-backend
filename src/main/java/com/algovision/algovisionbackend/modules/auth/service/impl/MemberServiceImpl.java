package com.algovision.algovisionbackend.modules.auth.service.impl;

import com.algovision.algovisionbackend.global.security.jwt.JwtProperties;
import com.algovision.algovisionbackend.global.security.jwt.JwtProvider;
import com.algovision.algovisionbackend.global.security.jwt.service.AuthTokenService;
import com.algovision.algovisionbackend.global.security.jwt.service.JwtRedisService;
import com.algovision.algovisionbackend.modules.auth.domain.Member;
import com.algovision.algovisionbackend.modules.auth.dto.*;
import com.algovision.algovisionbackend.modules.auth.exception.*;
import com.algovision.algovisionbackend.modules.auth.mapper.MemberMapper;
import com.algovision.algovisionbackend.modules.auth.repository.MemberRepository;
import com.algovision.algovisionbackend.modules.auth.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    private final PasswordEncoder passwordEncoder;
    private final JwtRedisService jwtRedisService;
    private final AuthTokenService authTokenService;
    private final JwtProperties jwtProperties;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public MemberResponse signup(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new DuplicateNicknameException(request.nickname());
        }

        Member member = Member.createWithEncodedPassword(request, passwordEncoder);
        Member saved = memberRepository.save(member);

        return memberMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findMemberByEmail(request.email())
                .orElseThrow(InvalidLoginException::new);

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new InvalidLoginException();
        }

        String accessToken = jwtProvider.generateAccessToken(member.getId());
        String refreshToken = jwtProvider.generateRefreshToken();

        jwtRedisService.saveRefreshToken(
                member.getId(),
                refreshToken,
                jwtProperties.getRefreshExpiration()
        );

        return new AuthResponse(memberMapper.toResponse(member), accessToken);
    }

    @Override
    @Transactional
    public void withdraw(String accessToken, Long memberId) {
        authTokenService.logout(accessToken, memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
        member.softDelete();
    }

    @Override
    @Transactional
    public MemberResponse changePassword(Long memberId, ChangePasswordRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        if (!passwordEncoder.matches(request.currentPassword(), member.getPasswordHash())) {
            throw new PasswordMismatchException();
        }

        if (passwordEncoder.matches(request.newPassword(), member.getPasswordHash())) {
            throw new SamePasswordException();
        }

        member.changePassword(passwordEncoder.encode(request.newPassword()));

        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public MemberResponse changeNickname(Long memberId, UpdateNicknameRequest request) {
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new DuplicateNicknameException(request.nickname());
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        member.updateNickname(request.nickname());
        return memberMapper.toResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return memberMapper.toResponse(member);
    }
}
