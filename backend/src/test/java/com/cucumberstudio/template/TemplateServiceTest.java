package com.cucumberstudio.template;

import com.cucumberstudio.template.dto.TemplateDtos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateServiceTest {
    private TemplateService service;

    @BeforeEach
    void setup() {
        service = new TemplateService(new InMemoryTemplateRepository());
    }

    @Test
    void shouldRunCrudAndApplyTemplate() {
        TemplateDtos.TemplateDto created = service.create(new TemplateDtos.TemplateDto(null, "tpl", "desc", List.of("a"), "SCENARIO", "Scenario: <name>", null, null));
        assertNotNull(created.id());

        TemplateDtos.TemplateDto updated = service.update(created.id(), new TemplateDtos.TemplateDto(created.id(), "tpl2", "desc2", List.of(), "SCENARIO", "Scenario: <name>", Instant.now(), Instant.now()));
        assertEquals("tpl2", updated.name());

        TemplateDtos.TemplateApplyResponseDto applied = service.apply(created.id(), new TemplateDtos.TemplateApplyRequestDto(Map.of("name", "Smoke"), null));
        assertTrue(applied.preview().contains("Smoke"));

        service.delete(created.id());
        assertThrows(TemplateException.class, () -> service.get(created.id()));
    }

    @Test
    void shouldValidateScope() {
        assertThrows(TemplateException.class, () -> service.create(new TemplateDtos.TemplateDto(null, "bad", "", List.of(), "BAD_SCOPE", "x", null, null)));
    }

    private static final class InMemoryTemplateRepository extends TemplateRepository {
        private final java.util.Map<String, TemplateDtos.TemplateDto> map = new java.util.LinkedHashMap<>();

        @Override
        public synchronized List<TemplateDtos.TemplateDto> findAll() {
            return new java.util.ArrayList<>(map.values());
        }

        @Override
        public synchronized java.util.Optional<TemplateDtos.TemplateDto> findById(String id) {
            return java.util.Optional.ofNullable(map.get(id));
        }

        @Override
        public synchronized TemplateDtos.TemplateDto save(TemplateDtos.TemplateDto template) {
            java.time.Instant now = java.time.Instant.now();
            String id = template.id() == null ? java.util.UUID.randomUUID().toString() : template.id();
            TemplateDtos.TemplateDto persisted = new TemplateDtos.TemplateDto(id, template.name(), template.description(), template.tags(), template.scope(), template.content(), template.createdAt() == null ? now : template.createdAt(), now);
            map.put(id, persisted);
            return persisted;
        }

        @Override
        public synchronized void delete(String id) {
            map.remove(id);
        }
    }
}
