package com.algovision.algovisionbackend.modules.email.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationCodeException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.BAD_REQUEST;

    public InvalidVerificationCodeException() {
        super("인증 코드가 일치하지 않거나 만료되었습니다.", STATUS_CODE);
    }
}
