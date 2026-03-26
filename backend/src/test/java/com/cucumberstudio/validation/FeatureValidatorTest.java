package com.cucumberstudio.validation;

import com.cucumberstudio.domain.*;
import com.cucumberstudio.parser.GherkinFeatureParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeatureValidatorTest {
    private final FeatureValidator validator = new FeatureValidator(new GherkinFeatureParser());

    @Test
    void shouldReportMissingColumnForOutlinePlaceholder() {
        FeatureDocument feature = new FeatureDocument();
        feature.setName("Login");

        ScenarioNode outline = new ScenarioNode();
        outline.setType(ScenarioType.OUTLINE);
        outline.setName("Acceso inválido");
        outline.getSteps().add(new StepNode(StepKeyword.WHEN, "introduzco <usuario> y <password>"));

        ExampleTable table = new ExampleTable();
        table.getColumns().add("usuario");
        outline.getExampleTables().add(table);

        feature.getScenarios().add(outline);

        ValidationResult result = validator.validate(feature);
        assertFalse(result.errors().isEmpty());
        assertTrue(result.missingColumns().contains("password"));
    }
}
