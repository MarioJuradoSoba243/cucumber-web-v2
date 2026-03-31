package com.cucumberstudio.service;

import com.cucumberstudio.config.CucumberProperties;
import com.cucumberstudio.dto.FeatureDtos;
import com.cucumberstudio.dto.FeatureStepValidationDtos;
import com.cucumberstudio.exporter.GherkinFeatureExporter;
import com.cucumberstudio.mapper.FeatureMapper;
import com.cucumberstudio.parser.GherkinFeatureParser;
import com.cucumberstudio.validation.FeatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StepValidationTest {
    @TempDir
    Path tempDir;

    private FeatureService service;

    @BeforeEach
    void setup() {
        CucumberProperties properties = new CucumberProperties();
        properties.setFeaturesPath(tempDir.toString());
        GherkinFeatureParser parser = new GherkinFeatureParser();
        service = new FeatureService(properties, parser, new GherkinFeatureExporter(), new FeatureMapper(), new FeatureValidator(parser));
    }

    @Test
    void shouldReportBlockingErrorsByStep() {
        FeatureDtos.FeatureDocumentDto dto = new FeatureDtos.FeatureDocumentDto(
                "id.feature",
                "",
                "Feature",
                "",
                List.of(),
                null,
                List.of(new FeatureDtos.ScenarioDto("s1", "OUTLINE", "Outline", List.of(), List.of(new FeatureDtos.StepDto("GIVEN", "value <missing>")), List.of())),
                null
        );

        var exampleStep = service.validateStep(dto, FeatureStepValidationDtos.ValidationStep.EXAMPLES);
        assertTrue(exampleStep.blocking());
        assertTrue(exampleStep.errors().stream().anyMatch(error -> error.contains("Outline")));
    }
}
