package com.cucumberstudio.template;

public class TemplateException extends RuntimeException {
    private final String code;

    public TemplateException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
