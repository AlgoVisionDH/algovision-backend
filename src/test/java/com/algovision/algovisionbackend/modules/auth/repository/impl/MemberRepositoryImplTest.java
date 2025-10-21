package com.algovision.algovisionbackend.modules.auth.repository.impl;

import com.algovision.algovisionbackend.config.QueryDslConfig;
import com.algovision.algovisionbackend.modules.auth.domain.Member;
import com.algovision.algovisionbackend.modules.auth.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@EnableJpaAuditing
class MemberRepositoryImplTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("회원을 저장하면 id, createdAt, updatedAt이 초기화된다.")
    void save_initializeIdAndCreatedAtAndUpdatedAt() {
        Member member = Member
                .builder()
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash("passwordHash")
                .build();
        memberRepository.save(member);

        em.flush();
        em.clear();

        assertThat(member.getId()).isNotNull();
        assertThat(member.getCreatedAt()).isNotNull();
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일로 회원을 조회하면 MemberResponse DTO로 반환된다.")
    void findByEmail_returnsMemberResponse() {
        Member member = Member
                .builder()
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash("passwordHash")
                .build();
        memberRepository.save(member);

        em.flush();
        em.clear();

        var result = memberRepository.findByEmail("test@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("닉네임으로 회원을 조회하면 MemberResponse DTO로 반환된다.")
    void findByNickname_returnsMemberResponse() {
        Member member = Member
                .builder()
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash("passwordHash")
                .build();
        memberRepository.save(member);

        em.flush();
        em.clear();

        var result = memberRepository.findByNickname("nickname");

        assertThat(result).isPresent();
        assertThat(result.get().nickname()).isEqualTo("nickname");
    }

    @Test
    @DisplayName("회원 정보 변경 시 updatedAt이 최신화된다.")
    void update_updateUpdatedAt() {
        Member member = Member
                .builder()
                .email("test@test.com")
                .nickname("nickname")
                .passwordHash("passwordHash")
                .build();
        memberRepository.save(member);

        em.flush();
        em.clear();

        Member saved = memberRepository.findById(member.getId()).get();
        var prevUpdatedAt = saved.getUpdatedAt();

        saved.updateNickname("newNickname");
        em.flush();
        em.clear();

        Member updated = memberRepository.findById(member.getId()).get();
        var curUpdatedAt = updated.getUpdatedAt();

        assertThat(updated.getNickname()).isEqualTo("newNickname");
        assertThat(prevUpdatedAt).isNotEqualTo(curUpdatedAt);
    }
}
