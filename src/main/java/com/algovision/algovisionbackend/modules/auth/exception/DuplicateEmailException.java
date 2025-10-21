package com.algovision.algovisionbackend.modules.auth.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.CONFLICT;

    public DuplicateEmailException(String email) {
        super(String.format("이미 존재하는 이메일입니다. (%s)",email), STATUS_CODE);
    }
}
