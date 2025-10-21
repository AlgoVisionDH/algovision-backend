package com.algovision.algovisionbackend.modules.auth.repository.impl;

import com.algovision.algovisionbackend.modules.auth.domain.QMember;
import com.algovision.algovisionbackend.modules.auth.domain.Role;
import com.algovision.algovisionbackend.modules.auth.dto.MemberResponse;
import com.algovision.algovisionbackend.modules.auth.dto.MemberSearchCondition;
import com.algovision.algovisionbackend.modules.auth.repository.CustomMemberRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements CustomMemberRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MemberResponse> findByEmail(String email) {
        QMember member = QMember.member;

        MemberResponse result = queryFactory
                .select(Projections.constructor(MemberResponse.class,
                        member.id,
                        member.email,
                        member.nickname,
                        member.role,
                        member.createdAt,
                        member.updatedAt
                ))
                .from(member)
                .where(member.email.eq(email))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<MemberResponse> findByNickname(String nickname) {
        QMember member = QMember.member;

        MemberResponse result = queryFactory
                .select(Projections.constructor(MemberResponse.class,
                        member.id,
                        member.email,
                        member.nickname,
                        member.role,
                        member.createdAt,
                        member.updatedAt
                ))
                .from(member)
                .where(member.nickname.eq(nickname))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<MemberResponse> search(MemberSearchCondition condition, Pageable pageable) {
        QMember member = QMember.member;

        List<MemberResponse> results = queryFactory
                .select(Projections.constructor(
                        MemberResponse.class,
                        member.id,
                        member.email,
                        member.nickname,
                        member.role,
                        member.createdAt,
                        member.updatedAt
                ))
                .from(member)
                .where(
                        emailContains(condition.getEmail()),
                        nicknameContains(condition.getNickname()),
                        eqRole(condition.getRole()),
                        eqDeleted(condition.getIsDeleted())
                )
                .orderBy(member.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        emailContains(condition.getEmail()),
                        nicknameContains(condition.getNickname()),
                        eqRole(condition.getRole()),
                        eqDeleted(condition.getIsDeleted())
                )
                .fetchOne();

        return new PageImpl<>(results, pageable, total == null ? 0 : total);
    }

    private BooleanExpression emailContains(String email) {
        return (email != null && !email.isBlank()) ? QMember.member.email.containsIgnoreCase(email) : null;
    }

    private BooleanExpression nicknameContains(String nickname) {
        return (nickname != null && !nickname.isBlank()) ? QMember.member.nickname.containsIgnoreCase(nickname) : null;
    }

    private BooleanExpression eqRole(Role role) {
        return role != null ? QMember.member.role.eq(role) : null;
    }

    private BooleanExpression eqDeleted(Boolean isDeleted) {
        return isDeleted != null ? QMember.member.isDeleted.eq(isDeleted) : null;
    }
}
