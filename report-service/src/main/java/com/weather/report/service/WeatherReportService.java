package com.weather.report.service;

import com.weather.common.model.WeatherReport;
import com.weather.provider.model.OpenWeatherResponse;
import com.weather.storage.service.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;


@Service
@Slf4j
@RequiredArgsConstructor
public class WeatherReportService {
    private final RestTemplate restTemplate;
    private final LocalStorageService storageService;
    private final String apiKey = System.getenv("OW_API_KEY");

    public WeatherReport getWeatherReport(String location, LocalDate date) {
        // Try to get from local storage first
        Optional<WeatherReport> cached = storageService.getWeatherReport(location, date);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Fetch from OpenWeather API if not in storage
        WeatherReport report = fetchFromOpenWeather(location, date);
        storageService.saveWeatherReport(location, date, report);
        return report;
    }

    public List<WeatherReport> getWeeklyReport(String location, LocalDate startDate) {
        List<WeatherReport> reports = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            reports.add(getWeatherReport(location, date));
        }
        return reports;
    }

    private WeatherReport fetchFromOpenWeather(String location, LocalDate date) {
        String url = String.format(
                "https://api.openweathermap.org/data/2.5/forecast?zip=%s,us&appid=%s&units=metric",
                location, apiKey
        );

        try {
            log.info("Fetching weather data for location: {}", location);
            OpenWeatherResponse response = restTemplate.getForObject(url, OpenWeatherResponse.class);
            if (response != null) {
                return mapToWeatherReport(response, location, date);
            } else {
                throw new RuntimeException("No response from weather service");
            }
        } catch (Exception e) {
            log.error("Failed to fetch weather data for {}", location, e);
            throw new RuntimeException("Failed to fetch weather data", e);
        }
    }

    private WeatherReport mapToWeatherReport(OpenWeatherResponse response, String location, LocalDate date) {
        if (response == null || response.getList() == null || response.getList().isEmpty()) {
            throw new RuntimeException("Invalid response from weather service");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        OpenWeatherResponse.WeatherData weatherData = response.getList().stream()
                .filter(data -> {
                    LocalDateTime forecastDateTime = LocalDateTime.parse(data.getDtTxt(), formatter);
                    return forecastDateTime.toLocalDate().equals(date);
                })
                .findFirst()
                // Fallback to first entry if no match
                .orElse(response.getList().get(0));

        OpenWeatherResponse.Main main = weatherData.getMain();
        OpenWeatherResponse.Wind wind = weatherData.getWind();

        WeatherReport report = new WeatherReport();
        report.setLocation(location);
        report.setDate(date);
        report.setHighTemp(main.getHighTemp());
        report.setLowTemp(main.getLowTemp());
        report.setHumidity(main.getHumidity());
        report.setWindSpeed(wind.getSpeed());
        report.setPrecipitationChance(calculatePrecipChance(weatherData));

        log.info("Mapped weather report for {} on {}: high={}, low={}",
                location, date, main.getHighTemp(), main.getLowTemp());

        return report;
    }

    private double calculatePrecipChance(OpenWeatherResponse.WeatherData weatherData) {
        if (weatherData.getRain() != null) {
            return weatherData.getRain().getThreeHours() > 0 ? 100.0 : 0.0;
        }
        return 0.0;
    }
}