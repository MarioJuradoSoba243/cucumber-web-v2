package com.cucumberstudio.validation;

import com.cucumberstudio.domain.*;
import com.cucumberstudio.parser.GherkinFeatureParser;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class FeatureValidator {
    private final GherkinFeatureParser parser;

    public FeatureValidator(GherkinFeatureParser parser) {
        this.parser = parser;
    }

    public ValidationResult validate(FeatureDocument document) {
        ValidationResult result = ValidationResult.empty();
        if (document.getName() == null || document.getName().isBlank()) {
            result.errors().add("Feature name cannot be empty");
        }
        for (ScenarioNode scenario : document.getScenarios()) {
            if (scenario.getName() == null || scenario.getName().isBlank()) {
                result.errors().add("Scenario name cannot be empty");
            }
            if (scenario.getType() == ScenarioType.OUTLINE) {
                validateOutline(scenario, result);
            }
        }
        return result;
    }

    private void validateOutline(ScenarioNode outline, ValidationResult result) {
        Set<String> placeholders = parser.collectPlaceholders(outline);
        Set<String> columns = new LinkedHashSet<>();
        for (ExampleTable table : outline.getExampleTables()) {
            if (table.getColumns().isEmpty()) {
                result.errors().add("Examples table without columns in outline: " + outline.getName());
            }
            columns.addAll(table.getColumns());
            table.getRows().forEach(row -> table.getColumns().forEach(column -> {
                String value = row.getValues().get(column);
                if (value == null || value.isBlank()) {
                    result.emptyCells().add(outline.getName() + " -> " + column);
                }
            }));
        }
        placeholders.stream().filter(p -> !columns.contains(p)).forEach(result.missingColumns()::add);
        columns.stream().filter(c -> !placeholders.contains(c)).forEach(result.unusedColumns()::add);

        if (!result.missingColumns().isEmpty()) {
            result.errors().add("Missing columns for placeholders: " + String.join(", ", result.missingColumns()));
        }
    }
}
