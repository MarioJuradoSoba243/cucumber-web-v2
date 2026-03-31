package com.cucumberstudio.search;

import com.cucumberstudio.config.CucumberProperties;
import com.cucumberstudio.parser.GherkinFeatureParser;
import com.cucumberstudio.search.dto.SearchDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchServiceTest {
    @TempDir
    Path tempDir;

    private SearchService searchService;

    @BeforeEach
    void setUp() {
        CucumberProperties properties = new CucumberProperties();
        properties.setFeaturesPath(tempDir.toString());
        searchService = new SearchService(properties, new GherkinFeatureParser());
    }

    @Test
    void shouldSearchByScenarioStepAndExampleValues() throws Exception {
        Files.writeString(tempDir.resolve("checkout.feature"), """
                Feature: Checkout
                  @smoke
                  Scenario Outline: pay
                    Given cart has <item>
                    Examples:
                      | item |
                      | book |
                """);

        SearchDtos.SearchPageDto stepResult = searchService.search(SearchDtos.SearchRequestDto.fromQuery("cart", List.of("outline"), List.of(), null, 0, 10));
        SearchDtos.SearchPageDto valueResult = searchService.search(SearchDtos.SearchRequestDto.fromQuery("book", List.of("example"), List.of(), null, 0, 10));

        assertEquals(1, stepResult.total());
        assertEquals(1, valueResult.total());
    }

    @Test
    void shouldSupportPaginationAndRanking() throws Exception {
        Files.writeString(tempDir.resolve("a.feature"), "Feature: Login\n  Scenario: login happy\n    Given login\n");
        Files.writeString(tempDir.resolve("b.feature"), "Feature: User login flows\n  Scenario: alt\n    Given login now\n");

        SearchDtos.SearchPageDto page = searchService.search(SearchDtos.SearchRequestDto.fromQuery("login", List.of("feature"), List.of(), null, 0, 1));

        assertEquals(2, page.total());
        assertEquals(1, page.results().size());
        assertTrue(page.results().getFirst().hits().getFirst().score() >= 95);
    }


    @Test
    void shouldReadLatin1EncodedFeatureFiles() throws Exception {
        Files.writeString(
                tempDir.resolve("latin1.feature"),
                "Feature: Búsqueda global\n  Scenario: consulta\n    Given término con acentos\n",
                StandardCharsets.ISO_8859_1
        );

        SearchDtos.SearchPageDto page = searchService.search(
                SearchDtos.SearchRequestDto.fromQuery("búsqueda", List.of("feature"), List.of(), null, 0, 10)
        );

        assertEquals(1, page.total());
    }

}
