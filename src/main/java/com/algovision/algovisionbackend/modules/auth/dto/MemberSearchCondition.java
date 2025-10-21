package com.algovision.algovisionbackend.modules.auth.dto;

import com.algovision.algovisionbackend.modules.auth.domain.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemberSearchCondition {
    private String email;
    private String nickname;
    private Role role;
    private Boolean isDeleted = false;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;
}
