package com.cucumberstudio.mapper;

import com.cucumberstudio.domain.*;
import com.cucumberstudio.dto.FeatureDtos;
import com.cucumberstudio.validation.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FeatureMapper {

    public FeatureDtos.FeatureSummaryDto toSummary(FeatureDocument document) {
        int outlineCount = (int) document.getScenarios().stream().filter(s -> s.getType() == ScenarioType.OUTLINE).count();
        int exampleRows = document.getScenarios().stream()
                .flatMap(s -> s.getExampleTables().stream())
                .mapToInt(t -> t.getRows().size())
                .sum();
        return new FeatureDtos.FeatureSummaryDto(
                document.getId(),
                document.getName(),
                document.getFilePath(),
                document.getDescription(),
                new ArrayList<>(document.getTags()),
                document.getScenarios().size(),
                outlineCount,
                exampleRows
        );
    }

    public FeatureDtos.FeatureDocumentDto toDto(FeatureDocument document, ValidationResult validationResult) {
        return new FeatureDtos.FeatureDocumentDto(
                document.getId(),
                document.getFilePath(),
                document.getName(),
                document.getDescription(),
                new ArrayList<>(document.getTags()),
                mapBackground(document.getBackground()),
                document.getScenarios().stream().map(this::mapScenario).toList(),
                new FeatureDtos.ValidationReportDto(
                        validationResult.errors(),
                        validationResult.warnings(),
                        validationResult.missingColumns(),
                        validationResult.unusedColumns(),
                        validationResult.emptyCells()
                )
        );
    }

    public FeatureDocument toDomain(FeatureDtos.FeatureDocumentDto dto) {
        FeatureDocument document = new FeatureDocument();
        document.setId(dto.id());
        document.setFilePath(dto.filePath());
        document.setName(dto.name());
        document.setDescription(dto.description());
        document.getTags().addAll(dto.tags() == null ? List.of() : dto.tags());
        if (dto.background() != null) {
            Background background = new Background();
            background.setName(dto.background().name());
            if (dto.background().steps() != null) {
                dto.background().steps().forEach(step -> background.getSteps().add(new StepNode(StepKeyword.valueOf(step.keyword()), step.text())));
            }
            document.setBackground(background);
        }
        if (dto.scenarios() != null) {
            dto.scenarios().forEach(scenarioDto -> {
                ScenarioNode scenario = new ScenarioNode();
                scenario.setId(scenarioDto.id());
                scenario.setType(ScenarioType.valueOf(scenarioDto.type()));
                scenario.setName(scenarioDto.name());
                scenario.getTags().addAll(scenarioDto.tags() == null ? List.of() : scenarioDto.tags());
                if (scenarioDto.steps() != null) {
                    scenarioDto.steps().forEach(step -> scenario.getSteps().add(new StepNode(StepKeyword.valueOf(step.keyword()), step.text())));
                }
                if (scenarioDto.exampleTables() != null) {
                    scenarioDto.exampleTables().forEach(tableDto -> {
                        ExampleTable table = new ExampleTable();
                        table.setId(tableDto.id());
                        table.setName(tableDto.name());
                        table.getTags().addAll(tableDto.tags() == null ? List.of() : tableDto.tags());
                        table.getColumns().addAll(tableDto.columns() == null ? List.of() : tableDto.columns());
                        if (tableDto.rows() != null) {
                            tableDto.rows().forEach(rowDto -> {
                                ExampleRow row = new ExampleRow();
                                row.setId(rowDto.id());
                                if (rowDto.values() != null) {
                                    row.getValues().putAll(rowDto.values());
                                }
                                table.getRows().add(row);
                            });
                        }
                        scenario.getExampleTables().add(table);
                    });
                }
                document.getScenarios().add(scenario);
            });
        }
        return document;
    }

    private FeatureDtos.BackgroundDto mapBackground(Background background) {
        if (background == null) {
            return null;
        }
        return new FeatureDtos.BackgroundDto(background.getName(), background.getSteps().stream().map(this::mapStep).toList());
    }

    private FeatureDtos.ScenarioDto mapScenario(ScenarioNode scenario) {
        return new FeatureDtos.ScenarioDto(
                scenario.getId(),
                scenario.getType().name(),
                scenario.getName(),
                new ArrayList<>(scenario.getTags()),
                scenario.getSteps().stream().map(this::mapStep).toList(),
                scenario.getExampleTables().stream().map(this::mapTable).toList()
        );
    }

    private FeatureDtos.StepDto mapStep(StepNode step) {
        return new FeatureDtos.StepDto(step.getKeyword().name(), step.getText());
    }

    private FeatureDtos.ExampleTableDto mapTable(ExampleTable table) {
        return new FeatureDtos.ExampleTableDto(
                table.getId(),
                table.getName(),
                new ArrayList<>(table.getTags()),
                new ArrayList<>(table.getColumns()),
                table.getRows().stream().map(row -> new FeatureDtos.ExampleRowDto(row.getId(), row.getValues())).toList()
        );
    }
}
