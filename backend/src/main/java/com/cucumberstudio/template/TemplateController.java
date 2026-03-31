package com.cucumberstudio.template;

import com.cucumberstudio.template.dto.TemplateDtos;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
@CrossOrigin(origins = "*")
public class TemplateController {
    private final TemplateService service;

    public TemplateController(TemplateService service) {
        this.service = service;
    }

    @GetMapping
    public List<TemplateDtos.TemplateDto> list() {
        return service.list();
    }

    @PostMapping
    public TemplateDtos.TemplateDto create(@Valid @RequestBody TemplateDtos.TemplateDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public TemplateDtos.TemplateDto get(@PathVariable String id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public TemplateDtos.TemplateDto update(@PathVariable String id, @Valid @RequestBody TemplateDtos.TemplateDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @PostMapping("/{id}/apply")
    public TemplateDtos.TemplateApplyResponseDto apply(@PathVariable String id, @RequestBody(required = false) TemplateDtos.TemplateApplyRequestDto request) {
        return service.apply(id, request);
    }
}
