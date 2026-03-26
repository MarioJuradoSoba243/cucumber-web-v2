package com.cucumberstudio.domain;

import java.util.ArrayList;
import java.util.List;

public class Background {
    private String name = "Background";
    private final List<StepNode> steps = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<StepNode> getSteps() {
        return steps;
    }
}
