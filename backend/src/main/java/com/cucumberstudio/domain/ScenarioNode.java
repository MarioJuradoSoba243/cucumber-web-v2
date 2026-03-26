package com.cucumberstudio.domain;

import java.util.ArrayList;
import java.util.List;

public class ScenarioNode {
    private String id;
    private ScenarioType type = ScenarioType.SCENARIO;
    private String name;
    private final List<String> tags = new ArrayList<>();
    private final List<StepNode> steps = new ArrayList<>();
    private final List<ExampleTable> exampleTables = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ScenarioType getType() {
        return type;
    }

    public void setType(ScenarioType type) {
        this.type = type;
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

    public List<StepNode> getSteps() {
        return steps;
    }

    public List<ExampleTable> getExampleTables() {
        return exampleTables;
    }
}
