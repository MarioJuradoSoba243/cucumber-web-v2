package com.cucumberstudio.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CucumberProperties.class)
public class AppConfig {
}
