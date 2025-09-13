package com.java2024.ecoscape.validation;

import java.util.List;

public class BusinessValidationException extends RuntimeException {
    private final List<String> errors;

    public BusinessValidationException(List<String> errors) {
        super(String.join("\n", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}