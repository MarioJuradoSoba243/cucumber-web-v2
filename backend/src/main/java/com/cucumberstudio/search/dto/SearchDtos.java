package com.cucumberstudio.search.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

public final class SearchDtos {
    private SearchDtos() {
    }

    public record SearchRequestDto(
            @NotBlank String q,
            List<String> types,
            List<String> tags,
            String path,
            @Min(0) int page,
            @Min(1) @Max(100) int size
    ) {
        public List<String> safeTypes() {
            return types == null ? List.of() : types;
        }

        public List<String> safeTags() {
            return tags == null ? List.of() : tags;
        }

        public static SearchRequestDto fromQuery(
                String q,
                List<String> types,
                List<String> tags,
                String path,
                Integer page,
                Integer size
        ) {
            return new SearchRequestDto(
                    q == null ? "" : q,
                    types == null ? new ArrayList<>() : types,
                    tags == null ? new ArrayList<>() : tags,
                    path,
                    page == null ? 0 : page,
                    size == null ? 10 : size
            );
        }
    }

    public record SearchLocationDto(
            String featureId,
            String scenarioId,
            String tableId,
            String rowId,
            String field
    ) {
    }

    public record SearchHitDto(
            String type,
            String text,
            String highlight,
            int score,
            SearchLocationDto location
    ) {
    }

    public record SearchResultDto(
            String featureId,
            String featureName,
            String filePath,
            List<SearchHitDto> hits
    ) {
    }

    public record SearchPageDto(
            List<SearchResultDto> results,
            long total,
            int page,
            int size
    ) {
    }
}
