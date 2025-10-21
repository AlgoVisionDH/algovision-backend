package com.algovision.algovisionbackend.modules.auth.repository;

import com.algovision.algovisionbackend.modules.auth.dto.MemberResponse;
import com.algovision.algovisionbackend.modules.auth.dto.MemberSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomMemberRepository {
    Optional<MemberResponse> findByEmail(String email);

    Optional<MemberResponse> findByNickname(String nickname);

    Page<MemberResponse> search(MemberSearchCondition condition, Pageable pageable);
}
