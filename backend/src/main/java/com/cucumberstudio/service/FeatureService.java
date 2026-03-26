package com.cucumberstudio.service;

import com.cucumberstudio.config.CucumberProperties;
import com.cucumberstudio.domain.FeatureDocument;
import com.cucumberstudio.dto.FeatureDtos;
import com.cucumberstudio.exception.NotFoundException;
import com.cucumberstudio.exporter.GherkinFeatureExporter;
import com.cucumberstudio.mapper.FeatureMapper;
import com.cucumberstudio.parser.GherkinFeatureParser;
import com.cucumberstudio.validation.FeatureValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class FeatureService {
    private static final Logger log = LoggerFactory.getLogger(FeatureService.class);
    private final CucumberProperties properties;
    private final GherkinFeatureParser parser;
    private final GherkinFeatureExporter exporter;
    private final FeatureMapper mapper;
    private final FeatureValidator validator;

    public FeatureService(CucumberProperties properties, GherkinFeatureParser parser, GherkinFeatureExporter exporter,
                          FeatureMapper mapper, FeatureValidator validator) {
        this.properties = properties;
        this.parser = parser;
        this.exporter = exporter;
        this.mapper = mapper;
        this.validator = validator;
    }

    public List<FeatureDtos.FeatureSummaryDto> listFeatures(String query) throws IOException {
        String safeQuery = query == null ? "" : query.toLowerCase();
        log.debug("Scanning features in {} with query {}", featuresPath(), safeQuery);
        try (var stream = Files.walk(featuresPath())) {
            return stream
                    .filter(path -> path.toString().endsWith(".feature"))
                    .sorted(Comparator.comparing(Path::toString))
                    .map(this::parseFileQuietly)
                    .filter(document -> matches(document, safeQuery))
                    .map(mapper::toSummary)
                    .toList();
        }
    }

    public FeatureDtos.FeatureDocumentDto getFeature(String id) throws IOException {
        Path path = resolveFromId(id);
        log.debug("Loading feature from {}", path);
        if (!Files.exists(path)) {
            throw new NotFoundException("Feature not found: " + id);
        }
        FeatureDocument doc = parser.parse(Files.readString(path), path);
        return mapper.toDto(doc, validator.validate(doc));
    }

    public FeatureDtos.FeatureDocumentDto createFeature(FeatureDtos.FeatureDocumentDto dto) throws IOException {
        FeatureDocument document = mapper.toDomain(dto);
        if (document.getScenarios().isEmpty()) {
            log.debug("Creating feature without scenarios yet: {}", document.getName());
        }
        if (document.getId() == null || document.getId().isBlank()) {
            document.setId(UUID.randomUUID() + ".feature");
        }
        Path path = resolveFromId(document.getId());
        if (!path.toString().endsWith(".feature")) {
            path = path.resolveSibling(path.getFileName() + ".feature");
        }
        document.setFilePath(path.toString());
        saveDomain(document, path);
        return mapper.toDto(document, validator.validate(document));
    }

    public FeatureDtos.FeatureDocumentDto updateFeature(String id, FeatureDtos.FeatureDocumentDto dto) throws IOException {
        Path path = resolveFromId(id);
        log.debug("Updating feature at {}", path);
        FeatureDocument document = mapper.toDomain(dto);
        document.setId(path.toString());
        document.setFilePath(path.toString());
        saveDomain(document, path);
        return mapper.toDto(document, validator.validate(document));
    }

    public void deleteFeature(String id) throws IOException {
        Path path = resolveFromId(id);
        log.debug("Deleting feature file {}", path);
        Files.deleteIfExists(path);
    }

    public String exportFeature(String id) throws IOException {
        Path path = resolveFromId(id);
        log.debug("Exporting feature file {}", path);
        FeatureDocument document = parser.parse(Files.readString(path), path);
        return exporter.export(document);
    }

    public String getFeaturesPath() {
        return featuresPath().toString();
    }

    public long rescanCount() throws IOException {
        try (var stream = Files.walk(featuresPath())) {
            return stream.filter(path -> path.toString().endsWith(".feature")).count();
        }
    }

    private FeatureDocument parseFileQuietly(Path path) {
        try {
            return parser.parse(Files.readString(path, StandardCharsets.UTF_8), path);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private boolean matches(FeatureDocument document, String query) {
        if (query.isBlank()) {
            return true;
        }
        return document.getName().toLowerCase().contains(query)
                || document.getFilePath().toLowerCase().contains(query)
                || document.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(query))
                || (document.getDescription() != null && document.getDescription().toLowerCase().contains(query));
    }

    private void saveDomain(FeatureDocument document, Path path) throws IOException {
        var validation = validator.validate(document);
        if (!validation.errors().isEmpty()) {
            log.warn("Validation errors for {}: {}", path, validation.errors());
            throw new IllegalArgumentException(String.join("; ", validation.errors()));
        }
        Files.createDirectories(path.getParent());
        log.info("Persisting feature to {}", path);
        Files.writeString(path, exporter.export(document), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Path resolveFromId(String id) {
        Path raw = Path.of(id);
        if (raw.isAbsolute()) {
            return raw;
        }
        return featuresPath().resolve(id).normalize();
    }

    private Path featuresPath() {
        return Path.of(properties.getFeaturesPath()).toAbsolutePath().normalize();
    }
}
