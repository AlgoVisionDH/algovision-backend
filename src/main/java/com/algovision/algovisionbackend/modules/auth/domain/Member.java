package com.algovision.algovisionbackend.modules.auth.domain;

import com.algovision.algovisionbackend.global.audit.BaseEntity;
import com.algovision.algovisionbackend.modules.auth.dto.SignUpRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Entity
@Table(name = "members",
        indexes = {
                @Index(name = "idx_member_email", columnList = "email", unique = true),
                @Index(name = "idx_member_nickname", columnList = "nickname", unique = true)
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = "passwordHash")
@Builder
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 60)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Builder
    private Member(String email, String nickname, String passwordHash){
        this.email = email;
        this.nickname = nickname;
        this.passwordHash = passwordHash;
    }

    public static Member createWithEncodedPassword(SignUpRequest request, PasswordEncoder encoder){
        return Member.builder()
                .email(request.email())
                .nickname(request.nickname())
                .passwordHash(encoder.encode(request.password()))
                .build();
    }

    public void changePassword(String newPasswordHash){
        this.passwordHash = newPasswordHash;
    }

    public void updateNickname(String newNickname){
        this.nickname = newNickname;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(!(obj instanceof Member)){
            return false;
        }
        Member other = (Member) obj;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
