package com.cucumberstudio.template;

import com.cucumberstudio.template.dto.TemplateDtos;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TemplateRepository {
    private static final Path STORAGE = Path.of("backend", "data", "templates.json").toAbsolutePath().normalize();
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public synchronized List<TemplateDtos.TemplateDto> findAll() {
        return readAll();
    }

    public synchronized Optional<TemplateDtos.TemplateDto> findById(String id) {
        return readAll().stream().filter(template -> template.id().equals(id)).findFirst();
    }

    public synchronized TemplateDtos.TemplateDto save(TemplateDtos.TemplateDto template) {
        List<TemplateDtos.TemplateDto> list = readAll();
        Instant now = Instant.now();
        TemplateDtos.TemplateDto normalized = new TemplateDtos.TemplateDto(
                template.id() == null || template.id().isBlank() ? UUID.randomUUID().toString() : template.id(),
                template.name(),
                template.description(),
                template.tags() == null ? List.of() : template.tags(),
                template.scope(),
                template.content(),
                template.createdAt() == null ? now : template.createdAt(),
                now
        );
        list.removeIf(existing -> existing.id().equals(normalized.id()));
        list.add(normalized);
        writeAll(list);
        return normalized;
    }

    public synchronized void delete(String id) {
        List<TemplateDtos.TemplateDto> list = readAll();
        list.removeIf(existing -> existing.id().equals(id));
        writeAll(list);
    }

    private List<TemplateDtos.TemplateDto> readAll() {
        try {
            if (!Files.exists(STORAGE)) {
                return new ArrayList<>();
            }
            String raw = Files.readString(STORAGE);
            if (raw.isBlank()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read templates storage", exception);
        }
    }

    private void writeAll(List<TemplateDtos.TemplateDto> data) {
        try {
            Files.createDirectories(STORAGE.getParent());
            Files.writeString(STORAGE, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to persist templates storage", exception);
        }
    }
}
