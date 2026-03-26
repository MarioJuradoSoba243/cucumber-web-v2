package com.cucumberstudio.domain;

import java.util.ArrayList;
import java.util.List;

public class ExampleTable {
    private String id;
    private String name;
    private final List<String> tags = new ArrayList<>();
    private final List<String> columns = new ArrayList<>();
    private final List<ExampleRow> rows = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<ExampleRow> getRows() {
        return rows;
    }
}
