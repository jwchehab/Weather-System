package com.weather.alert.config;

import com.weather.storage.service.LocalStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AlertServiceConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public LocalStorageService storageService() {
        return new LocalStorageService();
    }
}
