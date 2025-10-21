package com.algovision.algovisionbackend.modules.auth.mapper;

import com.algovision.algovisionbackend.modules.auth.domain.Member;
import com.algovision.algovisionbackend.modules.auth.dto.MemberResponse;
import com.algovision.algovisionbackend.modules.auth.dto.SignUpRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberMapper INSTANCE = Mappers.getMapper(MemberMapper.class);

    Member toEntity(SignUpRequest request);

    MemberResponse toResponse(Member member);
}
