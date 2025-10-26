package com.algovision.algovisionbackend.modules.email.exception;

import com.algovision.algovisionbackend.common.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MailSendFailedException extends BaseException {
    private static final HttpStatus STATUS_CODE = HttpStatus.INTERNAL_SERVER_ERROR;

    public MailSendFailedException() {
        super("이메일 전송에 실패했습니다.", STATUS_CODE);
    }
}
