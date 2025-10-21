package com.algovision.algovisionbackend.global.handler;

import com.algovision.algovisionbackend.common.exception.BaseException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Value("${app.log.includeStacktrace:true}")
    private boolean includeStacktrace;

    /**
     * 비즈니스 로직 예외 (BaseException 계열)
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        String traceId = getTraceId();

        if (ex.getStatus().is4xxClientError()) {
            log.warn("[TRACE:{}] [ClientError] [{}] {}",
                    traceId, ex.getClass().getSimpleName(), ex.getMessage());
        } else {
            if (includeStacktrace) {
                log.error("[TRACE:{}] [ServerError] [{}] {}",
                        traceId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
            } else {
                log.error("[TRACE:{}] [ServerError] [{}] {}",
                        traceId, ex.getClass().getSimpleName(), ex.getMessage());
            }
        }

        ErrorResponse response = new ErrorResponse(ex.getStatus().value(), ex.getMessage(), traceId);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * DTO @Valid 검증 실패 예외 (ex. @NotBlank, @Size 등)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String traceId = getTraceId();
        String message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();

        log.warn("[TRACE:{}] [ValidationError] [{}] {}",
                traceId, ex.getClass().getSimpleName(), message);

        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * @AssertTrue, @Validated 등 제약조건 위반 예외
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String traceId = getTraceId();
        String message = ex.getConstraintViolations().iterator().next().getMessage();

        log.warn("[TRACE:{}] [ConstraintViolation] [{}] {}",
                traceId, ex.getClass().getSimpleName(), message);

        ErrorResponse response = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), message, traceId);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 예상치 못한 모든 예외 처리 (최후방어선)
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Throwable ex) {
        String traceId = getTraceId();

        if (includeStacktrace) {
            log.error("[TRACE:{}] [UnexpectedError] [{}] {}",
                    traceId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        } else {
            log.error("[TRACE:{}] [UnexpectedError] [{}] {}",
                    traceId, ex.getClass().getSimpleName(), ex.getMessage());
        }

        ErrorResponse response = new ErrorResponse(500, "서버 내부 오류가 발생했습니다.", traceId);
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * 표준 에러 응답 포맷
     */
    public record ErrorResponse(int status, String message, String traceId) {}

    private String getTraceId() {
        return MDC.get("traceId");
    }
}
