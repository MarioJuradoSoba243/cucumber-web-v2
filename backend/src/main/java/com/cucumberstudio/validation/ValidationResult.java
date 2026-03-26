package com.cucumberstudio.validation;

import java.util.ArrayList;
import java.util.List;

public record ValidationResult(
        List<String> errors,
        List<String> warnings,
        List<String> missingColumns,
        List<String> unusedColumns,
        List<String> emptyCells
) {
    public static ValidationResult empty() {
        return new ValidationResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
}
