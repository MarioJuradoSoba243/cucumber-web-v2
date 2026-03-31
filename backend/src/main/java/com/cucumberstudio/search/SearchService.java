package com.cucumberstudio.search;

import com.cucumberstudio.config.CucumberProperties;
import com.cucumberstudio.domain.ExampleTable;
import com.cucumberstudio.domain.FeatureDocument;
import com.cucumberstudio.domain.ScenarioNode;
import com.cucumberstudio.domain.ScenarioType;
import com.cucumberstudio.parser.GherkinFeatureParser;
import com.cucumberstudio.search.dto.SearchDtos;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private static final Set<String> ALLOWED_TYPES = Set.of("feature", "scenario", "outline", "example");
    private final CucumberProperties properties;
    private final GherkinFeatureParser parser;

    public SearchService(CucumberProperties properties, GherkinFeatureParser parser) {
        this.properties = properties;
        this.parser = parser;
    }

    public SearchDtos.SearchPageDto search(SearchDtos.SearchRequestDto request) throws IOException {
        validateRequest(request);
        String q = request.q().trim().toLowerCase();
        List<String> types = request.safeTypes().stream().map(String::toLowerCase).toList();
        List<String> tags = request.safeTags().stream().map(String::toLowerCase).toList();
        String pathFilter = request.path() == null ? "" : request.path().toLowerCase();

        List<SearchHitContext> matches = new ArrayList<>();
        try (var stream = Files.walk(featuresPath())) {
            List<Path> files = stream.filter(path -> path.toString().endsWith(".feature")).toList();
            for (Path path : files) {
                if (!pathFilter.isBlank() && !path.toString().toLowerCase().contains(pathFilter)) {
                    continue;
                }
                FeatureDocument doc = parser.parse(Files.readString(path, StandardCharsets.UTF_8), path);
                collectMatches(doc, q, types, tags, matches);
            }
        }

        matches.sort(Comparator.comparingInt(SearchHitContext::score).reversed());
        int from = request.page() * request.size();
        int to = Math.min(from + request.size(), matches.size());
        if (from >= matches.size()) {
            return new SearchDtos.SearchPageDto(List.of(), matches.size(), request.page(), request.size());
        }
        List<SearchHitContext> pageHits = matches.subList(from, to);

        Map<String, List<SearchHitContext>> grouped = pageHits.stream().collect(Collectors.groupingBy(SearchHitContext::featureId, LinkedHashMap::new, Collectors.toList()));
        List<SearchDtos.SearchResultDto> results = grouped.values().stream().map(this::toResult).toList();

        return new SearchDtos.SearchPageDto(results, matches.size(), request.page(), request.size());
    }

    private void collectMatches(FeatureDocument doc, String q, List<String> types, List<String> tags, List<SearchHitContext> matches) {
        boolean filterByType = !types.isEmpty();
        boolean filterByTags = !tags.isEmpty();

        if (matchesType("feature", types, filterByType)) {
            addIfMatches(doc, q, doc.getName(), "feature", "name", null, null, null, null, 100, filterByTags, tags, doc.getTags(), matches);
            addIfMatches(doc, q, doc.getDescription(), "feature", "description", null, null, null, null, 50, filterByTags, tags, doc.getTags(), matches);
            doc.getTags().forEach(tag -> addIfMatches(doc, q, tag, "feature", "tag", null, null, null, null, 40, filterByTags, tags, doc.getTags(), matches));
        }

        for (ScenarioNode scenario : doc.getScenarios()) {
            String scenarioType = scenario.getType() == ScenarioType.OUTLINE ? "outline" : "scenario";
            if (matchesType(scenarioType, types, filterByType)) {
                addIfMatches(doc, q, scenario.getName(), scenarioType, "name", scenario.getId(), null, null, null, 90, filterByTags, tags, scenario.getTags(), matches);
                scenario.getTags().forEach(tag -> addIfMatches(doc, q, tag, scenarioType, "tag", scenario.getId(), null, null, null, 35, filterByTags, tags, scenario.getTags(), matches));
                scenario.getSteps().forEach(step -> addIfMatches(doc, q, step.getText(), scenarioType, "step", scenario.getId(), null, null, null, 30, filterByTags, tags, scenario.getTags(), matches));
            }
            if (matchesType("example", types, filterByType)) {
                for (ExampleTable table : scenario.getExampleTables()) {
                    table.getTags().forEach(tag -> addIfMatches(doc, q, tag, "example", "tag", scenario.getId(), table.getId(), null, null, 25, filterByTags, tags, table.getTags(), matches));
                    table.getColumns().forEach(column -> addIfMatches(doc, q, column, "example", "column", scenario.getId(), table.getId(), null, column, 20, filterByTags, tags, table.getTags(), matches));
                    table.getRows().forEach(row -> row.getValues().forEach((column, value) -> addIfMatches(
                            doc, q, value, "example", "value", scenario.getId(), table.getId(), row.getId(), column, 10, filterByTags, tags, table.getTags(), matches
                    )));
                }
            }
        }
    }

    private boolean matchesType(String type, List<String> selectedTypes, boolean filterByType) {
        return !filterByType || selectedTypes.contains(type);
    }

    private void addIfMatches(
            FeatureDocument doc,
            String q,
            String text,
            String type,
            String field,
            String scenarioId,
            String tableId,
            String rowId,
            String locationField,
            int baseScore,
            boolean filterByTags,
            List<String> requiredTags,
            List<String> sourceTags,
            List<SearchHitContext> sink
    ) {
        if (text == null || text.isBlank()) {
            return;
        }
        String lowerText = text.toLowerCase();
        if (!lowerText.contains(q)) {
            return;
        }
        if (filterByTags && requiredTags.stream().noneMatch(tag -> sourceTags.stream().map(String::toLowerCase).toList().contains(tag))) {
            return;
        }
        int score = lowerText.equals(q) ? baseScore : baseScore - 5;
        String highlight = highlight(text, q);
        sink.add(new SearchHitContext(
                doc.getId(),
                doc.getName(),
                doc.getFilePath(),
                new SearchDtos.SearchHitDto(
                        type,
                        text,
                        highlight,
                        score,
                        new SearchDtos.SearchLocationDto(doc.getId(), scenarioId, tableId, rowId, locationField == null ? field : locationField)
                )
        ));
    }

    private SearchDtos.SearchResultDto toResult(List<SearchHitContext> contexts) {
        SearchHitContext first = contexts.getFirst();
        List<SearchDtos.SearchHitDto> hits = contexts.stream().map(SearchHitContext::hit).toList();
        return new SearchDtos.SearchResultDto(first.featureId(), first.featureName(), first.filePath(), hits);
    }

    private String highlight(String text, String query) {
        String lowerText = text.toLowerCase();
        int index = lowerText.indexOf(query);
        if (index < 0) {
            return text;
        }
        return text.substring(0, index) + "<mark>" + text.substring(index, index + query.length()) + "</mark>" + text.substring(index + query.length());
    }

    private void validateRequest(SearchDtos.SearchRequestDto request) {
        if (request.q() == null || request.q().trim().isBlank()) {
            throw new IllegalArgumentException("q is required");
        }
        if (request.page() < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (request.size() < 1 || request.size() > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        for (String type : request.safeTypes()) {
            if (!ALLOWED_TYPES.contains(type.toLowerCase())) {
                throw new IllegalArgumentException("Invalid search type: " + type);
            }
        }
    }

    private Path featuresPath() {
        return Path.of(properties.getFeaturesPath()).toAbsolutePath().normalize();
    }

    private record SearchHitContext(String featureId, String featureName, String filePath, SearchDtos.SearchHitDto hit) {
        int score() {
            return hit.score();
        }
    }
}
