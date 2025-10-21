package com.algovision.algovisionbackend.modules.auth.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class SamePasswordException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.CONFLICT;

    public SamePasswordException() {
        super("새 비밀번호는 현재 비밀번호와 달라야 합니다.", STATUS_CODE);
    }
}
