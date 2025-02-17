package com.weather.statistics.service;

import com.weather.common.model.WeatherReport;
import com.weather.storage.service.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {
    private final RestTemplate restTemplate;
    private final LocalStorageService storageService;

    public com.weather.common.model.WeatherStatistics calculateStatistics(
            String location,
            LocalDate startDate,
            LocalDate endDate,
            List<String> metrics) {

        // Try to get from local storage first
        Optional<com.weather.common.model.WeatherStatistics> cached =
                storageService.getStatistics(location, startDate, endDate);
        if (cached.isPresent()) {
            log.info("Returning cached statistics for {} from {} to {}",
                    location, startDate, endDate);
            return cached.get();
        }

        // Fetch all required reports
        List<WeatherReport> reports = fetchReports(location, startDate, endDate);
        log.info("Fetched {} reports for {} from {} to {}",
                reports.size(), location, startDate, endDate);

        // Calculate statistics
        com.weather.common.model.WeatherStatistics stats = new com.weather.common.model.WeatherStatistics();
        stats.setLocation(location);
        stats.setStartDate(startDate);
        stats.setEndDate(endDate);
        stats.setCalculated(LocalDateTime.now());

        if (metrics.contains("temps")) {
            stats.setAverageTemperature(calculateAverageTemp(reports));
        }
        if (metrics.contains("precipitate")) {
            stats.setAveragePrecipitation(calculateAveragePrecip(reports));
        }
        if (metrics.contains("wind")) {
            stats.setAverageWindSpeed(calculateAverageWind(reports));
        }
        if (metrics.contains("humidity")) {
            stats.setAverageHumidity(calculateAverageHumidity(reports));
        }

        // Save to local storage with both dates
        storageService.saveStatistics(location, startDate, endDate, stats);
        log.info("Saved new statistics to cache for {} from {} to {}",
                location, startDate, endDate);

        return stats;
    }

    private List<WeatherReport> fetchReports(String location, LocalDate startDate, LocalDate endDate) {
        List<WeatherReport> reports = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            try {
                String url = String.format(
                        "http://localhost:8081/api/weather/report?location=%s&date=%s",
                        location, current
                );
                log.debug("Fetching report from: {}", url);
                WeatherReport report = restTemplate.getForObject(url, WeatherReport.class);
                if (report != null) {
                    reports.add(report);
                    log.debug("Received report for {}: {}", current, report);
                }
            } catch (Exception e) {
                log.error("Failed to fetch report for {} with error: {}", current, e.getMessage());
            }
            current = current.plusDays(1);
        }

        return reports;
    }

    private double calculateAverageTemp(List<WeatherReport> reports) {
        if (reports.isEmpty()) {
            log.warn("No reports available for temperature calculation");
            return 0.0;
        }
        return reports.stream()
                .mapToDouble(r -> (r.getHighTemp() + r.getLowTemp()) / 2)
                .average()
                .orElse(0.0);
    }

    private double calculateAveragePrecip(List<WeatherReport> reports) {
        if (reports.isEmpty()) {
            log.warn("No reports available for precipitation calculation");
            return 0.0;
        }
        return reports.stream()
                .mapToDouble(WeatherReport::getPrecipitationChance)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageWind(List<WeatherReport> reports) {
        if (reports.isEmpty()) {
            log.warn("No reports available for wind calculation");
            return 0.0;
        }
        return reports.stream()
                .mapToDouble(WeatherReport::getWindSpeed)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageHumidity(List<WeatherReport> reports) {
        if (reports.isEmpty()) {
            log.warn("No reports available for humidity calculation");
            return 0.0;
        }
        return reports.stream()
                .mapToDouble(WeatherReport::getHumidity)
                .average()
                .orElse(0.0);
    }
}