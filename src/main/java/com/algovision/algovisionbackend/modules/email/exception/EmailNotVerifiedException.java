package com.algovision.algovisionbackend.modules.email.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.FORBIDDEN;

    public EmailNotVerifiedException(String email) {
        super(String.format("이메일 [%s] 인증이 완료되지 않았습니다.", email), STATUS_CODE);
    }
}
