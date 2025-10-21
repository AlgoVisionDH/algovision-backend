package com.algovision.algovisionbackend.modules.auth.repository;

import com.algovision.algovisionbackend.modules.auth.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, CustomMemberRepository {
    Optional<Member> findMemberByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);
}