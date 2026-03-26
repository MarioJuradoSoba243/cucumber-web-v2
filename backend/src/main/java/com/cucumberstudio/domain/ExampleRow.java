package com.cucumberstudio.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public class ExampleRow {
    private String id;
    private final Map<String, String> values = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getValues() {
        return values;
    }
}
