package com.algovision.algovisionbackend.modules.auth.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.NOT_FOUND;

    public MemberNotFoundException() {
        super("존재하지 않는 회원입니다", STATUS_CODE);
    }
}
