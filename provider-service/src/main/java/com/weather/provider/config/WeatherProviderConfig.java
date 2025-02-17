package com.weather.provider.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WeatherProviderConfig {

    @Value("${provider.api-key}")
    private String apiKey;

    @Value("${provider.base-url}")
    private String baseUrl;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public WeatherApiProperties weatherApiProperties() {
        return new WeatherApiProperties(apiKey, baseUrl);
    }
}