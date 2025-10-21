package com.algovision.algovisionbackend.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NicknameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nickname {

    String message() default "{validation.nickname}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min() default 2;

    int max() default 20;

    boolean allowBlank() default false;
}
