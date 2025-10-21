package com.algovision.algovisionbackend.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NicknameValidator implements ConstraintValidator<Nickname, String> {
    private static final Pattern ALLOWED=Pattern.compile("^[a-zA-Z0-9가-힣_\\-\\.]+$");

    private int min;
    private int max;
    private boolean allowBlank;

    @Override
    public void initialize(Nickname constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.allowBlank = constraintAnnotation.allowBlank();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if(s == null){
            return true;
        }

        if(s.isBlank()){
            return allowBlank;
        }

        int len = s.length();
        if(len < min || len > max){
            return false;
        }

        return ALLOWED.matcher(s).matches();
    }
}
