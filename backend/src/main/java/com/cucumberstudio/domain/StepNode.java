package com.cucumberstudio.domain;

import java.util.Objects;

public class StepNode {
    private StepKeyword keyword;
    private String text;

    public StepNode() {
    }

    public StepNode(StepKeyword keyword, String text) {
        this.keyword = keyword;
        this.text = text;
    }

    public StepKeyword getKeyword() {
        return keyword;
    }

    public void setKeyword(StepKeyword keyword) {
        this.keyword = keyword;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StepNode stepNode)) {
            return false;
        }
        return keyword == stepNode.keyword && Objects.equals(text, stepNode.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword, text);
    }
}
