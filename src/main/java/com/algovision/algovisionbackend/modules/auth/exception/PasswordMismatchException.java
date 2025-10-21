package com.algovision.algovisionbackend.modules.auth.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PasswordMismatchException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.BAD_REQUEST;

    public PasswordMismatchException() {
        super("현재 비밀번호가 일치하지 않습니다.", STATUS_CODE);
    }
}
