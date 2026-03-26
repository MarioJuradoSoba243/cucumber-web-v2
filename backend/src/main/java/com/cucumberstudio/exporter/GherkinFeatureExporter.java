package com.cucumberstudio.exporter;

import com.cucumberstudio.domain.*;
import org.springframework.stereotype.Component;

@Component
public class GherkinFeatureExporter {

    public String export(FeatureDocument document) {
        StringBuilder builder = new StringBuilder();
        appendTags(builder, document.getTags(), 0);
        builder.append("Feature: ").append(document.getName()).append("\n");

        if (document.getDescription() != null && !document.getDescription().isBlank()) {
            for (String line : document.getDescription().split("\\R")) {
                builder.append("  ").append(line).append("\n");
            }
            builder.append("\n");
        }

        if (document.getBackground() != null) {
            builder.append("  Background: ");
            if (document.getBackground().getName() != null && !"Background".equals(document.getBackground().getName())) {
                builder.append(document.getBackground().getName());
            }
            builder.append("\n");
            document.getBackground().getSteps().forEach(step -> builder
                    .append("    ").append(step.getKeyword().value()).append(" ").append(step.getText()).append("\n"));
            builder.append("\n");
        }

        for (ScenarioNode scenario : document.getScenarios()) {
            appendTags(builder, scenario.getTags(), 2);
            builder.append("  ");
            if (scenario.getType() == ScenarioType.OUTLINE) {
                builder.append("Scenario Outline: ");
            } else {
                builder.append("Scenario: ");
            }
            builder.append(scenario.getName()).append("\n");

            scenario.getSteps().forEach(step -> builder
                    .append("    ").append(step.getKeyword().value()).append(" ").append(step.getText()).append("\n"));

            if (scenario.getType() == ScenarioType.OUTLINE) {
                builder.append("\n");
                for (ExampleTable table : scenario.getExampleTables()) {
                    appendTags(builder, table.getTags(), 4);
                    builder.append("    Examples:");
                    if (table.getName() != null && !table.getName().isBlank()) {
                        builder.append(" ").append(table.getName());
                    }
                    builder.append("\n");
                    appendTableRow(builder, table.getColumns(), 6);
                    for (ExampleRow row : table.getRows()) {
                        appendTableRow(builder, table.getColumns().stream().map(column -> row.getValues().getOrDefault(column, "")).toList(), 6);
                    }
                    builder.append("\n");
                }
            }
            builder.append("\n");
        }
        return builder.toString().stripTrailing() + "\n";
    }

    private void appendTags(StringBuilder builder, java.util.List<String> tags, int indent) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        builder.append(" ".repeat(indent));
        tags.forEach(tag -> builder.append("@").append(tag).append(" "));
        builder.setLength(builder.length() - 1);
        builder.append("\n");
    }

    private void appendTableRow(StringBuilder builder, java.util.List<String> values, int indent) {
        builder.append(" ".repeat(indent)).append("| ");
        for (String value : values) {
            builder.append(value == null ? "" : value).append(" | ");
        }
        builder.setLength(builder.length() - 1);
        builder.append("\n");
    }
}
