package com.cucumberstudio.template;

import com.cucumberstudio.template.dto.TemplateDtos;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([a-zA-Z0-9_\\-]+)>");
    private final TemplateRepository repository;

    public TemplateService(TemplateRepository repository) {
        this.repository = repository;
    }

    public List<TemplateDtos.TemplateDto> list() {
        return repository.findAll();
    }

    public TemplateDtos.TemplateDto get(String id) {
        return repository.findById(id).orElseThrow(() -> new TemplateException("TEMPLATE_NOT_FOUND", "Template not found: " + id));
    }

    public TemplateDtos.TemplateDto create(@Valid TemplateDtos.TemplateDto dto) {
        validateScope(dto.scope());
        return repository.save(dto);
    }

    public TemplateDtos.TemplateDto update(String id, @Valid TemplateDtos.TemplateDto dto) {
        TemplateDtos.TemplateDto existing = get(id);
        validateScope(dto.scope());
        return repository.save(new TemplateDtos.TemplateDto(
                existing.id(),
                dto.name(),
                dto.description(),
                dto.tags(),
                dto.scope(),
                dto.content(),
                existing.createdAt(),
                existing.updatedAt()
        ));
    }

    public void delete(String id) {
        get(id);
        repository.delete(id);
    }

    public TemplateDtos.TemplateApplyResponseDto apply(String id, TemplateDtos.TemplateApplyRequestDto request) {
        TemplateDtos.TemplateDto template = get(id);
        validateScope(template.scope());
        Map<String, String> placeholders = request == null || request.placeholders() == null ? Map.of() : request.placeholders();
        String preview = fillPlaceholders(template.content(), placeholders);
        if (TemplateDtos.TemplateScope.fromText(template.scope()) == TemplateDtos.TemplateScope.OUTLINE
                && !preview.contains("Examples:")) {
            throw new TemplateException("INVALID_TEMPLATE_SCOPE", "Outline templates must contain examples section");
        }
        return new TemplateDtos.TemplateApplyResponseDto(template.scope(), preview, placeholders);
    }

    private String fillPlaceholders(String content, Map<String, String> placeholders) {
        if (content == null) {
            return "";
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(content);
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = placeholders.get(key);
            if (value == null) {
                throw new TemplateException("MISSING_PLACEHOLDER", "Missing value for placeholder: " + key);
            }
            matcher.appendReplacement(output, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private void validateScope(String scope) {
        try {
            TemplateDtos.TemplateScope.fromText(scope);
        } catch (Exception exception) {
            throw new TemplateException("INVALID_TEMPLATE_SCOPE", "Invalid template scope: " + scope);
        }
    }
}
