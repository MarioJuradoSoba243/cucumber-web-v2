package com.cucumberstudio.template.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class TemplateDtos {
    private TemplateDtos() {
    }

    public enum TemplateScope {
        FEATURE,
        SCENARIO,
        OUTLINE;

        public static TemplateScope fromText(String text) {
            return TemplateScope.valueOf(text.toUpperCase());
        }
    }

    public record TemplateDto(
            String id,
            @NotBlank String name,
            String description,
            List<String> tags,
            @NotBlank String scope,
            String content,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record TemplateApplyRequestDto(
            Map<String, String> placeholders,
            String targetName
    ) {
    }

    public record TemplateApplyResponseDto(
            String scope,
            String preview,
            Map<String, String> placeholders
    ) {
    }

    public record TemplateErrorDto(
            String code,
            String message
    ) {
    }
}
