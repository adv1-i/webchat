package com.example.webchat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.FormContentFilter;

@Configuration
public class FormConfig {

    @Bean
    public FormContentFilter formContentFilter() {
        return new FormContentFilter();
    }
}
