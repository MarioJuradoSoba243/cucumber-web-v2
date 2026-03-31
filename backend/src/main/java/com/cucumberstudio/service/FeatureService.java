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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FeatureService {
    private static final Logger log = LoggerFactory.getLogger(FeatureService.class);
    private static final List<Charset> FEATURE_CHARSETS = List.of(StandardCharsets.UTF_8, StandardCharsets.ISO_8859_1);
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
            List<Path> featurePaths = stream
                    .filter(path -> path.toString().endsWith(".feature"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
            List<FeatureDtos.FeatureSummaryDto> summaries = new ArrayList<>();
            for (Path featurePath : featurePaths) {
                Optional<FeatureDocument> maybeDocument = parseFileQuietly(featurePath);
                if (maybeDocument.isEmpty()) {
                    continue;
                }
                FeatureDocument document = maybeDocument.get();
                if (matches(document, safeQuery)) {
                    summaries.add(mapper.toSummary(document));
                }
            }
            return summaries;
        }
    }

    public FeatureDtos.FeatureDocumentDto getFeature(String id) throws IOException {
        Path path = resolveFromId(id);
        log.debug("Loading feature from {}", path);
        if (!Files.exists(path)) {
            throw new NotFoundException("Feature not found: " + id);
        }
        FeatureDocument doc = parseFile(path);
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
        FeatureDocument document = parseFile(path);
        return exporter.export(document);
    }

    public String exportSelected(FeatureDtos.ExportSelectionRequest request) {
        if (request == null || request.feature() == null) {
            throw new IllegalArgumentException("Feature payload is required for custom export");
        }
        FeatureDocument document = mapper.toDomain(request.feature());
        Set<String> scenarioIds = request.safeScenarios().stream()
                .filter(FeatureDtos.ScenarioExportSelectionDto::selected)
                .map(FeatureDtos.ScenarioExportSelectionDto::scenarioId)
                .collect(java.util.stream.Collectors.toSet());

        Map<String, Set<String>> rowIdsByTable = new HashMap<>();
        request.safeScenarios().forEach(selection -> selection.safeExampleRowsByTable().forEach((tableId, rowIds) -> {
            if (rowIds == null) {
                return;
            }
            rowIdsByTable.put(tableId, Set.copyOf(rowIds));
        }));

        return exporter.export(document, new GherkinFeatureExporter.ExportSelection(scenarioIds, rowIdsByTable));
    }

    public String getFeaturesPath() {
        return featuresPath().toString();
    }

    public long rescanCount() throws IOException {
        try (var stream = Files.walk(featuresPath())) {
            return stream.filter(path -> path.toString().endsWith(".feature")).count();
        }
    }

    public FeatureDtos.DirectoryNodeDto getDirectoryTree() throws IOException {
        Path root = featuresPath();
        Files.createDirectories(root);
        return buildDirectoryNode(root, root);
    }

    public FeatureDtos.DirectoryNodeDto createFolder(FeatureDtos.CreateFolderRequestDto request) throws IOException {
        Path parent = resolveDirectoryPath(request.parentPath());
        Path newFolder = parent.resolve(sanitizeName(request.name())).normalize();
        ensureInsideRoot(newFolder);
        Files.createDirectories(newFolder);
        return getDirectoryTree();
    }

    public FeatureDtos.DirectoryNodeDto renamePath(FeatureDtos.RenamePathRequestDto request) throws IOException {
        Path source = resolvePath(request.path());
        if (!Files.exists(source)) {
            throw new NotFoundException("Path not found: " + request.path());
        }
        Path target = source.resolveSibling(sanitizeName(request.newName())).normalize();
        ensureInsideRoot(target);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        return getDirectoryTree();
    }

    public FeatureDtos.DirectoryNodeDto movePath(FeatureDtos.MovePathRequestDto request) throws IOException {
        Path source = resolvePath(request.sourcePath());
        if (!Files.exists(source)) {
            throw new NotFoundException("Path not found: " + request.sourcePath());
        }
        Path destinationFolder = resolveDirectoryPath(request.destinationFolderPath());
        Path target = destinationFolder.resolve(source.getFileName()).normalize();
        ensureInsideRoot(target);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        return getDirectoryTree();
    }

    private Optional<FeatureDocument> parseFileQuietly(Path path) {
        try {
            return Optional.of(parseFile(path));
        } catch (IOException ex) {
            log.warn("Skipping unreadable feature file {}: {}", path, ex.getMessage());
            return Optional.empty();
        }
    }

    private boolean matches(FeatureDocument document, String query) {
        if (query.isBlank()) {
            return true;
        }
        String name = document.getName() == null ? "" : document.getName().toLowerCase();
        return name.contains(query)
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
        return resolvePath(id);
    }

    private Path featuresPath() {
        return Path.of(properties.getFeaturesPath()).toAbsolutePath().normalize();
    }

    private FeatureDtos.DirectoryNodeDto buildDirectoryNode(Path nodePath, Path root) throws IOException {
        List<Path> children;
        try (var stream = Files.list(nodePath)) {
            children = stream.sorted(Comparator.comparing(Path::toString)).toList();
        }
        List<FeatureDtos.DirectoryNodeDto> folders = new ArrayList<>();
        for (Path child : children) {
            if (Files.isDirectory(child)) {
                folders.add(buildDirectoryNode(child, root));
            }
        }
        List<FeatureDtos.FeatureSummaryDto> features = new ArrayList<>();
        for (Path child : children) {
            if (!child.toString().endsWith(".feature")) {
                continue;
            }
            parseFileQuietly(child).ifPresent(feature -> features.add(mapper.toSummary(feature)));
        }
        return new FeatureDtos.DirectoryNodeDto(
                toRelative(root, nodePath).isBlank() ? "/" : toRelative(root, nodePath),
                nodePath.getFileName() == null ? root.getFileName().toString() : nodePath.getFileName().toString(),
                toRelative(root, nodePath),
                folders,
                features
        );
    }

    private Path resolvePath(String rawPath) {
        Path candidate = Path.of(rawPath);
        Path resolved = candidate.isAbsolute() ? candidate.normalize() : featuresPath().resolve(candidate).normalize();
        ensureInsideRoot(resolved);
        return resolved;
    }

    private Path resolveDirectoryPath(String rawPath) {
        Path path = resolvePath(rawPath);
        if (Files.exists(path) && !Files.isDirectory(path)) {
            throw new IllegalArgumentException("Destination is not a folder: " + rawPath);
        }
        return path;
    }

    private void ensureInsideRoot(Path path) {
        Path root = featuresPath();
        if (!path.startsWith(root)) {
            throw new IllegalArgumentException("Path outside features root is not allowed");
        }
    }

    private String toRelative(Path root, Path target) {
        if (root.equals(target)) {
            return "";
        }
        return root.relativize(target).toString().replace('\\', '/');
    }

    private String sanitizeName(String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (clean.contains("/") || clean.contains("\\")) {
            throw new IllegalArgumentException("Name must not contain path separators");
        }
        return clean;
    }

    private FeatureDocument parseFile(Path path) throws IOException {
        String content = readFeatureContent(path);
        FeatureDocument document = parser.parse(content, path);
        if (document.getName() == null || document.getName().isBlank()) {
            throw new IOException("Feature name is missing in file " + path);
        }
        return document;
    }

    private String readFeatureContent(Path path) throws IOException {
        IOException lastException = null;
        for (Charset charset : FEATURE_CHARSETS) {
            try {
                return Files.readString(path, charset);
            } catch (CharacterCodingException ex) {
                lastException = new IOException("Cannot decode feature with charset " + charset, ex);
            }
        }
        if (lastException != null) {
            throw lastException;
        }
        throw new IOException("Unable to read feature file: " + path);
    }
}
