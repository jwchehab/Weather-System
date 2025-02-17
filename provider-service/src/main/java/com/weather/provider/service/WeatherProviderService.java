package com.weather.provider.service;

import com.weather.provider.config.WeatherApiProperties;
import com.weather.common.model.WeatherReport;
import com.weather.provider.exception.WeatherDataException;
import com.weather.provider.model.OpenWeatherResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherProviderService {

    private final RestTemplate restTemplate;
    private final WeatherApiProperties apiProperties;

    @Cacheable(value = "weatherData", key = "#location")
    @Retryable(value = WeatherDataException.class,
            backoff = @Backoff(delay = 1000))
    public WeatherReport getCurrentWeather(String location) {
        String url = buildUrl("/weather", location);
        try {
            OpenWeatherResponse response = restTemplate.getForObject(url, OpenWeatherResponse.class);
            return mapToWeatherReport(response);
        } catch (Exception e) {
            log.error("Error fetching current weather for location: {}", location, e);
            throw new WeatherDataException("Failed to fetch weather data", e);
        }
    }

    @Cacheable(value = "forecastData", key = "#location")
    public List<WeatherReport> getWeeklyForecast(String location) {
        String url = buildUrl("/forecast", location);
        try {
            OpenWeatherResponse response =
                    restTemplate.getForObject(url, OpenWeatherResponse.class);
            return mapToWeeklyForecast(response);
        } catch (Exception e) {
            log.error("Error fetching forecast for location: {}", location, e);
            throw new WeatherDataException("Failed to fetch forecast data", e);
        }
    }

    @Cacheable(value = "historicalData",
            key = "#location + #date.toString()")
    public WeatherReport getHistoricalWeather(String location, LocalDate date) {
        String url = buildHistoricalUrl(location, date);
        try {
            OpenWeatherResponse response =
                    restTemplate.getForObject(url, OpenWeatherResponse.class);
            return mapToWeatherReport(response);
        } catch (Exception e) {
            log.error("Error fetching historical weather for location: {} and date: {}",
                    location, date, e);
            throw new WeatherDataException("Failed to fetch historical data", e);
        }
    }

    private String buildUrl(String endpoint, String location) {
        return String.format("%s%s?q=%s&appid=%s&units=metric",
                apiProperties.getBaseUrl(),
                endpoint,
                location,
                apiProperties.getApiKey()
        );
    }

    private String buildHistoricalUrl(String location, LocalDate date) {
        return String.format("%s/timemachine?q=%s&dt=%s&appid=%s&units=metric",
                apiProperties.getBaseUrl(),
                location,
                date.toEpochDay(),
                apiProperties.getApiKey()
        );
    }

    private WeatherReport mapToWeatherReport(OpenWeatherResponse response) {
        WeatherReport report = new WeatherReport();
        report.setDate(LocalDate.now());
        report.setLowTemp(response.getMain().getLowTemp());
        report.setHighTemp(response.getMain().getHighTemp());
        report.setPrecipitationChance(calculatePrecipitation(response));
        report.setWindSpeed(response.getWind().getSpeed());
        report.setWindDirection(calculateWindDirection(response.getWind().getDeg()));
        return report;
    }

    private List<WeatherReport> mapToWeeklyForecast(OpenWeatherResponse response) {
        List<WeatherReport> forecasts = new ArrayList<>();
        // Group by day and map to WeatherReport objects
        // Implementation details...
        return forecasts;
    }

    private double calculatePrecipitation(OpenWeatherResponse response) {
        // Calculate precipitation chance based on various factors
        // Implementation details...
        return 0.0;
    }

    private String calculateWindDirection(double degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round(degrees / 45.0) % 8;
        return directions[index];
    }
}
