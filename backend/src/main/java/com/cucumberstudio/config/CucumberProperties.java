package com.cucumberstudio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cucumber")
public class CucumberProperties {
    private String featuresPath = "./features";

    public String getFeaturesPath() {
        return featuresPath;
    }

    public void setFeaturesPath(String featuresPath) {
        this.featuresPath = featuresPath;
    }
}
