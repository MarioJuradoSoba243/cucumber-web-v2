package com.cucumberstudio.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FeatureDtos {
    private FeatureDtos() {
    }

    public record FeatureSummaryDto(
            String id,
            String name,
            String filePath,
            String description,
            List<String> tags,
            int scenarioCount,
            int outlineCount,
            int totalExampleRows
    ) {
    }

    public record FeatureDocumentDto(
            String id,
            String filePath,
            @NotBlank String name,
            String description,
            List<String> tags,
            BackgroundDto background,
            List<ScenarioDto> scenarios,
            ValidationReportDto validation
    ) {
    }

    public record BackgroundDto(String name, List<StepDto> steps) {
    }

    public record ScenarioDto(
            String id,
            @NotBlank String type,
            @NotBlank String name,
            List<String> tags,
            List<StepDto> steps,
            List<ExampleTableDto> exampleTables
    ) {
    }

    public record StepDto(@NotBlank String keyword, @NotBlank String text) {
    }

    public record ExampleTableDto(
            String id,
            String name,
            List<String> tags,
            List<String> columns,
            List<ExampleRowDto> rows
    ) {
    }

    public record ExampleRowDto(String id, Map<String, String> values) {
    }

    public record ValidationReportDto(
            List<String> errors,
            List<String> warnings,
            List<String> missingColumns,
            List<String> unusedColumns,
            List<String> emptyCells
    ) {
    }

    public static FeatureDocumentDto empty() {
        return new FeatureDocumentDto(
                null,
                null,
                "",
                "",
                new ArrayList<>(),
                null,
                new ArrayList<>(),
                new ValidationReportDto(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
        );
    }

    public static ExampleRowDto newRow(String id, List<String> columns) {
        Map<String, String> values = new LinkedHashMap<>();
        columns.forEach(column -> values.put(column, ""));
        return new ExampleRowDto(id, values);
    }
}
