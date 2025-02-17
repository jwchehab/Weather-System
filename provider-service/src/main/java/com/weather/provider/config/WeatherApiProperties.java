package com.weather.provider.config;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherApiProperties {
    private String apiKey;
    private String baseUrl;
}
