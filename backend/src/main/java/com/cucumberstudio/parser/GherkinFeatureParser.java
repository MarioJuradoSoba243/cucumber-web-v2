package com.cucumberstudio.parser;

import com.cucumberstudio.domain.*;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GherkinFeatureParser {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([^>]+)>");

    public FeatureDocument parse(String content, Path path) {
        FeatureDocument document = new FeatureDocument();
        document.setId(path.toString());
        document.setFilePath(path.toString());

        ScenarioNode currentScenario = null;
        ExampleTable currentTable = null;
        List<String> pendingTags = new ArrayList<>();
        List<String> descriptionLines = new ArrayList<>();
        boolean inFeatureDescription = false;

        for (String rawLine : content.split("\\R", -1)) {
            String line = rawLine.stripTrailing();
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) {
                continue;
            }
            if (trimmed.isEmpty()) {
                if (inFeatureDescription) {
                    descriptionLines.add("");
                }
                continue;
            }
            if (trimmed.startsWith("@")) {
                pendingTags.addAll(parseTags(trimmed));
                continue;
            }
            if (trimmed.startsWith("Feature:")) {
                document.setName(trimmed.substring("Feature:".length()).trim());
                document.getTags().addAll(pendingTags);
                pendingTags.clear();
                inFeatureDescription = true;
                continue;
            }
            if (trimmed.startsWith("Background:")) {
                inFeatureDescription = false;
                flushDescription(document, descriptionLines);
                Background background = new Background();
                String name = trimmed.substring("Background:".length()).trim();
                if (!name.isBlank()) {
                    background.setName(name);
                }
                document.setBackground(background);
                currentScenario = null;
                currentTable = null;
                continue;
            }
            if (trimmed.startsWith("Scenario Outline:") || trimmed.startsWith("Scenario:")) {
                inFeatureDescription = false;
                flushDescription(document, descriptionLines);
                currentTable = null;
                currentScenario = new ScenarioNode();
                currentScenario.setId(UUID.randomUUID().toString());
                if (trimmed.startsWith("Scenario Outline:")) {
                    currentScenario.setType(ScenarioType.OUTLINE);
                    currentScenario.setName(trimmed.substring("Scenario Outline:".length()).trim());
                } else {
                    currentScenario.setType(ScenarioType.SCENARIO);
                    currentScenario.setName(trimmed.substring("Scenario:".length()).trim());
                }
                currentScenario.getTags().addAll(pendingTags);
                pendingTags.clear();
                document.getScenarios().add(currentScenario);
                continue;
            }
            if (trimmed.startsWith("Examples:")) {
                if (currentScenario == null) {
                    continue;
                }
                currentTable = new ExampleTable();
                currentTable.setId(UUID.randomUUID().toString());
                String name = trimmed.substring("Examples:".length()).trim();
                currentTable.setName(name.isBlank() ? null : name);
                currentTable.getTags().addAll(pendingTags);
                pendingTags.clear();
                currentScenario.getExampleTables().add(currentTable);
                continue;
            }
            if (trimmed.startsWith("|") && trimmed.endsWith("|") && currentTable != null) {
                List<String> cells = parseTableCells(trimmed);
                if (currentTable.getColumns().isEmpty()) {
                    currentTable.getColumns().addAll(cells);
                } else {
                    ExampleRow row = new ExampleRow();
                    row.setId(UUID.randomUUID().toString());
                    for (int i = 0; i < currentTable.getColumns().size(); i++) {
                        String column = currentTable.getColumns().get(i);
                        String value = i < cells.size() ? cells.get(i) : "";
                        row.getValues().put(column, value);
                    }
                    currentTable.getRows().add(row);
                }
                continue;
            }
            if (isStep(trimmed)) {
                StepKeyword keyword = StepKeyword.fromLine(trimmed);
                String text = trimmed.substring(keyword.value().length()).trim();
                StepNode step = new StepNode(keyword, text);
                if (currentScenario != null) {
                    currentScenario.getSteps().add(step);
                } else if (document.getBackground() != null) {
                    document.getBackground().getSteps().add(step);
                }
                continue;
            }

            if (inFeatureDescription) {
                descriptionLines.add(trimmed);
            }
        }
        flushDescription(document, descriptionLines);
        return document;
    }

    public Set<String> collectPlaceholders(ScenarioNode outline) {
        Set<String> placeholders = new LinkedHashSet<>();
        outline.getSteps().forEach(step -> {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(step.getText());
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }
        });
        return placeholders;
    }

    private List<String> parseTags(String line) {
        return Arrays.stream(line.split("\\s+"))
                .filter(token -> token.startsWith("@"))
                .map(token -> token.substring(1))
                .toList();
    }

    private List<String> parseTableCells(String line) {
        String body = line.substring(1, line.length() - 1);
        List<String> cells = new ArrayList<>();
        StringBuilder cellBuilder = new StringBuilder();
        for (int i = 0; i < body.length(); i++) {
            char current = body.charAt(i);
            if (current == '\\' && i + 1 < body.length() && body.charAt(i + 1) == '|') {
                cellBuilder.append('|');
                i++;
                continue;
            }
            if (current == '|') {
                cells.add(normalizeCellValue(cellBuilder.toString().trim()));
                cellBuilder.setLength(0);
                continue;
            }
            cellBuilder.append(current);
        }
        cells.add(normalizeCellValue(cellBuilder.toString().trim()));
        return cells;
    }

    private String normalizeCellValue(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            String unquoted = value.substring(1, value.length() - 1);
            return unquoted.replace("\\\"", "\"");
        }
        return value;
    }

    private boolean isStep(String line) {
        return line.startsWith("Given ") || line.startsWith("When ") || line.startsWith("Then ") || line.startsWith("And ") || line.startsWith("But ");
    }

    private void flushDescription(FeatureDocument document, List<String> lines) {
        if (document.getDescription() == null && !lines.isEmpty()) {
            document.setDescription(String.join("\n", lines).strip());
            lines.clear();
        }
    }
}
