package com.algovision.algovisionbackend.modules.email.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TooManyEmailRequestsException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.TOO_MANY_REQUESTS;

    public TooManyEmailRequestsException() {
        super("너무 많은 이메일 요청이 감지되었습니다. 잠시 후 다시 시도해주세요.", STATUS_CODE);
    }
}
