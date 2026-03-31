package com.cucumberstudio.dto;

import java.util.List;

public final class FeatureStepValidationDtos {
    private FeatureStepValidationDtos() {
    }

    public enum ValidationStep {
        FEATURE,
        SCENARIOS,
        EXAMPLES,
        FINAL;

        public static ValidationStep fromParam(String step) {
            if (step == null || step.isBlank()) {
                return FINAL;
            }
            return ValidationStep.valueOf(step.trim().toUpperCase());
        }
    }

    public record StepValidationResponse(
            List<String> errors,
            List<String> warnings,
            boolean blocking
    ) {
    }
}
