package com.cucumberstudio.parser;

import com.cucumberstudio.domain.ScenarioType;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GherkinFeatureParserTest {
    private final GherkinFeatureParser parser = new GherkinFeatureParser();

    @Test
    void shouldParseScenarioOutlineWithMultipleExamples() {
        String text = """
                @checkout
                Feature: Checkout
                  Scenario Outline: Calcular total
                    Given un carrito con <items> artículos
                    Then el total es <total>

                    @basic
                    Examples: Casos básicos
                      | items | total |
                      | 1     | 10    |

                    Examples: Casos extremos
                      | items | total |
                      | "100" | "500" |
                """;

        var doc = parser.parse(text, Path.of("checkout.feature"));
        assertEquals("Checkout", doc.getName());
        assertEquals(1, doc.getScenarios().size());
        var outline = doc.getScenarios().getFirst();
        assertEquals(ScenarioType.OUTLINE, outline.getType());
        assertEquals(2, outline.getExampleTables().size());
        assertEquals("Casos básicos", outline.getExampleTables().get(0).getName());
        assertEquals("500", outline.getExampleTables().get(1).getRows().getFirst().getValues().get("total"));
        assertEquals("100", outline.getExampleTables().get(1).getRows().getFirst().getValues().get("items"));
        assertTrue(parser.collectPlaceholders(outline).contains("items"));
    }

    @Test
    void shouldParseEscapedPipeCharacterInExampleValues() {
        String text = """
                Feature: Checkout
                  Scenario Outline: Leer valor con pipe
                    Given el valor "<valor>"

                    Examples:
                      | valor              |
                      | "A\\|B\\|C"        |
                      | dato\\|sin\\|comillas |
                """;

        var doc = parser.parse(text, Path.of("checkout.feature"));
        var rows = doc.getScenarios().getFirst().getExampleTables().getFirst().getRows();

        assertEquals("A|B|C", rows.get(0).getValues().get("valor"));
        assertEquals("dato|sin|comillas", rows.get(1).getValues().get("valor"));
    }
}
