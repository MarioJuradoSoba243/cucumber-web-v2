package com.cucumberstudio.exporter;

import com.cucumberstudio.domain.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GherkinFeatureExporter {

    public String export(FeatureDocument document) {
        return export(document, null);
    }

    public String export(FeatureDocument document, ExportSelection selection) {
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

        Map<String, Set<String>> selectedRowsByTableId = selection == null ? Map.of() : selection.selectedRowsByTableId();
        for (ScenarioNode scenario : document.getScenarios()) {
            if (selection != null && !selection.selectedScenarioIds().contains(scenario.getId())) {
                continue;
            }
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
                    Set<String> selectedRows = selectedRowsByTableId.get(table.getId());
                    appendTags(builder, table.getTags(), 4);
                    builder.append("    Examples:");
                    if (table.getName() != null && !table.getName().isBlank()) {
                        builder.append(" ").append(table.getName());
                    }
                    builder.append("\n");
                    appendTableRow(builder, table.getColumns(), 6, false);
                    for (ExampleRow row : table.getRows()) {
                        if (selectedRows != null && !selectedRows.contains(row.getId())) {
                            continue;
                        }
                        appendTableRow(
                                builder,
                                table.getColumns().stream().map(column -> row.getValues().getOrDefault(column, "")).toList(),
                                6,
                                true
                        );
                    }
                    builder.append("\n");
                }
            }
            builder.append("\n");
        }
        return builder.toString().stripTrailing() + "\n";
    }

    public record ExportSelection(
            Set<String> selectedScenarioIds,
            Map<String, Set<String>> selectedRowsByTableId
    ) {
        public static ExportSelection from(Map<String, List<String>> scenarioIdsByTableId, List<String> scenarioIds) {
            Map<String, Set<String>> rowIds = new HashMap<>();
            scenarioIdsByTableId.forEach((tableId, values) -> rowIds.put(tableId, Set.copyOf(values)));
            return new ExportSelection(Set.copyOf(scenarioIds), rowIds);
        }
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

    private void appendTableRow(StringBuilder builder, java.util.List<String> values, int indent, boolean quoteValues) {
        builder.append(" ".repeat(indent)).append("| ");
        for (String value : values) {
            String finalValue = value == null ? "" : value;
            builder.append(quoteValues ? quoteExampleValue(finalValue) : finalValue).append(" | ");
        }
        builder.setLength(builder.length() - 1);
        builder.append("\n");
    }

    private String quoteExampleValue(String value) {
        if (value.isBlank()) {
            return "\"\"";
        }
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value;
        }
        return "\"" + value
                .replace("|", "\\|")
                .replace("\"", "\\\"") + "\"";
    }
}
