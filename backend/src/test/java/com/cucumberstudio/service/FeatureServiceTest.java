package com.cucumberstudio.service;

import com.cucumberstudio.config.CucumberProperties;
import com.cucumberstudio.dto.FeatureDtos;
import com.cucumberstudio.exporter.GherkinFeatureExporter;
import com.cucumberstudio.mapper.FeatureMapper;
import com.cucumberstudio.parser.GherkinFeatureParser;
import com.cucumberstudio.validation.FeatureValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureServiceTest {
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
    void shouldListFeaturesFromDisk() throws Exception {
        Files.writeString(tempDir.resolve("login.feature"), "Feature: Login\n\n  Scenario: ok\n    Given done\n");
        List<FeatureDtos.FeatureSummaryDto> features = service.listFeatures("login");
        assertEquals(1, features.size());
        assertEquals("Login", features.getFirst().name());
    }

    @Test
    void shouldBuildTreeCreateRenameAndMovePaths() throws Exception {
        Files.createDirectories(tempDir.resolve("auth"));
        Files.writeString(tempDir.resolve("auth/login.feature"), "Feature: Login\n");

        FeatureDtos.DirectoryNodeDto tree = service.getDirectoryTree();
        assertEquals(1, tree.folders().size());
        assertEquals("auth", tree.folders().getFirst().name());

        service.createFolder(new FeatureDtos.CreateFolderRequestDto("auth", "api"));
        assertTrue(Files.exists(tempDir.resolve("auth/api")));

        service.renamePath(new FeatureDtos.RenamePathRequestDto("auth/login.feature", "signin.feature"));
        assertTrue(Files.exists(tempDir.resolve("auth/signin.feature")));

        service.movePath(new FeatureDtos.MovePathRequestDto("auth/signin.feature", "auth/api"));
        assertTrue(Files.exists(tempDir.resolve("auth/api/signin.feature")));
    }
}
