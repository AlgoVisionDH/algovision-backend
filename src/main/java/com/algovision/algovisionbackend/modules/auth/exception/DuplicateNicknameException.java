package com.algovision.algovisionbackend.modules.auth.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DuplicateNicknameException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.CONFLICT;

    public DuplicateNicknameException(String nickname) {
        super(String.format("이미 존재하는 닉네임입니다. (%s)",nickname), STATUS_CODE);
    }
}
