package com.algovision.algovisionbackend.modules.auth.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidLoginException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.UNAUTHORIZED;

    public InvalidLoginException() {
        super("이메일 또는 비밀번호가 일치하지 않습니다.", STATUS_CODE);
    }
}
