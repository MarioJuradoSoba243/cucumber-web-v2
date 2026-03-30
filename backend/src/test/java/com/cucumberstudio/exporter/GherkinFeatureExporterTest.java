package com.cucumberstudio.exporter;

import com.cucumberstudio.domain.ExampleRow;
import com.cucumberstudio.domain.ExampleTable;
import com.cucumberstudio.domain.FeatureDocument;
import com.cucumberstudio.domain.ScenarioNode;
import com.cucumberstudio.domain.ScenarioType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GherkinFeatureExporterTest {

    private final GherkinFeatureExporter exporter = new GherkinFeatureExporter();

    @Test
    void shouldAutomaticallyQuoteExampleValues() {
        ExampleRow row = new ExampleRow();
        row.setId("row-1");
        row.getValues().putAll(Map.of(
                "username", "alice",
                "password", "secret | \"safe\" pass"
        ));

        ExampleTable table = new ExampleTable();
        table.setId("table-1");
        table.setName("Credenciales");
        table.getColumns().addAll(List.of("username", "password"));
        table.getRows().add(row);

        ScenarioNode outline = new ScenarioNode();
        outline.setId("scenario-1");
        outline.setName("Login");
        outline.setType(ScenarioType.OUTLINE);
        outline.getExampleTables().add(table);

        FeatureDocument document = new FeatureDocument();
        document.setId("id-1");
        document.setName("Login");
        document.setDescription("");
        document.setFilePath(Path.of("login.feature").toString());
        document.getScenarios().add(outline);

        String exported = exporter.export(document);

        assertTrue(exported.contains("| username | password |"));
        assertTrue(exported.contains("| \"alice\" | \"secret \\| \\\"safe\\\" pass\" |"));
    }
}
