package com.cucumberstudio.domain;

import java.util.ArrayList;
import java.util.List;

public class FeatureDocument {
    private String id;
    private String filePath;
    private String name;
    private String description;
    private final List<String> tags = new ArrayList<>();
    private Background background;
    private final List<ScenarioNode> scenarios = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public List<ScenarioNode> getScenarios() {
        return scenarios;
    }
}
