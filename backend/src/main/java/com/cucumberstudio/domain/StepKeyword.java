package com.cucumberstudio.domain;

public enum StepKeyword {
    GIVEN("Given"),
    WHEN("When"),
    THEN("Then"),
    AND("And"),
    BUT("But");

    private final String value;

    StepKeyword(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static StepKeyword fromLine(String line) {
        for (StepKeyword keyword : values()) {
            if (line.startsWith(keyword.value + " ")) {
                return keyword;
            }
        }
        throw new IllegalArgumentException("Invalid step keyword: " + line);
    }
}
