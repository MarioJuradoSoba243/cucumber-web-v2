package com.cucumberstudio.controller;

import com.cucumberstudio.dto.FeatureDtos;
import com.cucumberstudio.service.FeatureService;
import jakarta.validation.Valid;
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
    private final FeatureService service;

    public FeatureController(FeatureService service) {
        this.service = service;
    }

    @GetMapping("/features")
    public List<FeatureDtos.FeatureSummaryDto> list(@RequestParam(required = false) String q) throws IOException {
        return service.listFeatures(q);
    }

    @GetMapping("/features/{id}")
    public FeatureDtos.FeatureDocumentDto get(@PathVariable String id) throws IOException {
        return service.getFeature(id);
    }

    @PostMapping("/features")
    public FeatureDtos.FeatureDocumentDto create(@Valid @RequestBody FeatureDtos.FeatureDocumentDto dto) throws IOException {
        return service.createFeature(dto);
    }

    @PutMapping("/features/{id}")
    public FeatureDtos.FeatureDocumentDto update(@PathVariable String id, @Valid @RequestBody FeatureDtos.FeatureDocumentDto dto) throws IOException {
        return service.updateFeature(id, dto);
    }

    @DeleteMapping("/features/{id}")
    public void delete(@PathVariable String id) throws IOException {
        service.deleteFeature(id);
    }

    @PostMapping("/features/{id}/save")
    public FeatureDtos.FeatureDocumentDto save(@PathVariable String id, @Valid @RequestBody FeatureDtos.FeatureDocumentDto dto) throws IOException {
        return service.updateFeature(id, dto);
    }

    @GetMapping("/features/{id}/export")
    public ResponseEntity<String> export(@PathVariable String id) throws IOException {
        String content = service.exportFeature(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=feature.feature")
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    @PostMapping("/import/rescan")
    public Map<String, Long> rescan() throws IOException {
        return Map.of("count", service.rescanCount());
    }

    @GetMapping("/settings/features-path")
    public Map<String, String> path() {
        return Map.of("featuresPath", service.getFeaturesPath());
    }
}
