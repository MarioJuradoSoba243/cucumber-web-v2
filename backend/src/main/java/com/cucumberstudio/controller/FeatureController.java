package com.cucumberstudio.controller;

import com.cucumberstudio.dto.FeatureDtos;
import com.cucumberstudio.service.FeatureService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FeatureController {
    private static final Logger log = LoggerFactory.getLogger(FeatureController.class);
    private final FeatureService service;

    public FeatureController(FeatureService service) {
        this.service = service;
    }

    @GetMapping("/features")
    public List<FeatureDtos.FeatureSummaryDto> list(@RequestParam(required = false) String q) throws IOException {
        log.info("Listing features with query={}", q);
        return service.listFeatures(q);
    }

    @GetMapping("/features/{id}")
    public FeatureDtos.FeatureDocumentDto get(@PathVariable String id) throws IOException {
        log.info("Loading feature by pathVariable id={}", id);
        return service.getFeature(id);
    }

    @GetMapping("/features/detail")
    public FeatureDtos.FeatureDocumentDto getByQuery(@RequestParam String id) throws IOException {
        log.info("Loading feature by query id={}", id);
        return service.getFeature(id);
    }

    @PostMapping("/features")
    public FeatureDtos.FeatureDocumentDto create(@Valid @RequestBody FeatureDtos.FeatureDocumentDto dto) throws IOException {
        log.info("Creating feature name={}", dto.name());
        return service.createFeature(dto);
    }

    @PutMapping("/features/{id}")
    public FeatureDtos.FeatureDocumentDto update(@PathVariable String id, @Valid @RequestBody FeatureDtos.FeatureDocumentDto dto) throws IOException {
        log.info("Updating feature by pathVariable id={}", id);
        return service.updateFeature(id, dto);
    }

    @PutMapping("/features/detail")
    public FeatureDtos.FeatureDocumentDto updateByQuery(@RequestParam String id, @Valid @RequestBody FeatureDtos.FeatureDocumentDto dto) throws IOException {
        log.info("Updating feature by query id={}", id);
        return service.updateFeature(id, dto);
    }

    @DeleteMapping("/features/{id}")
    public void delete(@PathVariable String id) throws IOException {
        log.info("Deleting feature by pathVariable id={}", id);
        service.deleteFeature(id);
    }

    @DeleteMapping("/features/detail")
    public void deleteByQuery(@RequestParam String id) throws IOException {
        log.info("Deleting feature by query id={}", id);
        service.deleteFeature(id);
    }

    @PostMapping("/features/{id}/save")
    public FeatureDtos.FeatureDocumentDto save(@PathVariable String id, @Valid @RequestBody FeatureDtos.FeatureDocumentDto dto) throws IOException {
        log.info("Saving feature by pathVariable id={}", id);
        return service.updateFeature(id, dto);
    }

    @PostMapping("/features/save")
    public FeatureDtos.FeatureDocumentDto saveByQuery(@RequestParam String id, @Valid @RequestBody FeatureDtos.FeatureDocumentDto dto) throws IOException {
        log.info("Saving feature by query id={}", id);
        return service.updateFeature(id, dto);
    }

    @GetMapping("/features/{id}/export")
    public ResponseEntity<String> export(@PathVariable String id) throws IOException {
        log.info("Exporting feature by pathVariable id={}", id);
        String content = service.exportFeature(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feature.feature")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @GetMapping("/features/export")
    public ResponseEntity<String> exportByQuery(@RequestParam String id) throws IOException {
        log.info("Exporting feature by query id={}", id);
        String content = service.exportFeature(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feature.feature")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @PostMapping("/features/export")
    public ResponseEntity<String> customExport(@RequestBody FeatureDtos.ExportSelectionRequest request) {
        log.info("Exporting selected feature subset");
        String content = service.exportSelected(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feature.feature")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @PostMapping("/import/rescan")
    public Map<String, Long> rescan() throws IOException {
        log.info("Rescanning features directory");
        return Map.of("count", service.rescanCount());
    }

    @GetMapping("/settings/features-path")
    public Map<String, String> path() {
        log.info("Returning features path settings");
        return Map.of("featuresPath", service.getFeaturesPath());
    }

    @GetMapping("/folders/tree")
    public FeatureDtos.DirectoryNodeDto tree() throws IOException {
        log.info("Loading features tree");
        return service.getDirectoryTree();
    }

    @PostMapping("/folders")
    public FeatureDtos.DirectoryNodeDto createFolder(@Valid @RequestBody FeatureDtos.CreateFolderRequestDto request) throws IOException {
        log.info("Creating folder '{}' in '{}'", request.name(), request.parentPath());
        return service.createFolder(request);
    }

    @PutMapping("/paths/rename")
    public FeatureDtos.DirectoryNodeDto renamePath(@Valid @RequestBody FeatureDtos.RenamePathRequestDto request) throws IOException {
        log.info("Renaming path '{}' to '{}'", request.path(), request.newName());
        return service.renamePath(request);
    }

    @PostMapping("/paths/move")
    public FeatureDtos.DirectoryNodeDto movePath(@Valid @RequestBody FeatureDtos.MovePathRequestDto request) throws IOException {
        log.info("Moving '{}' into '{}'", request.sourcePath(), request.destinationFolderPath());
        return service.movePath(request);
    }
}
