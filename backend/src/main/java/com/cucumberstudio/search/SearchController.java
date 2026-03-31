package com.cucumberstudio.search;

import com.cucumberstudio.search.dto.SearchDtos;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public SearchDtos.SearchPageDto search(
            @RequestParam String q,
            @RequestParam(required = false) List<String> types,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) throws IOException {
        SearchDtos.SearchRequestDto request = SearchDtos.SearchRequestDto.fromQuery(q, types, tags, path, page, size);
        return searchService.search(request);
    }
}
