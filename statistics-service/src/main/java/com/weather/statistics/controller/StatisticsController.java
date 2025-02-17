package com.weather.statistics.controller;

import com.weather.statistics.model.StatisticsRequest;
import com.weather.common.model.WeatherStatistics;
import com.weather.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/statistics")
@Slf4j
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;

    @PostMapping
    public ResponseEntity<WeatherStatistics> calculateStatistics(
            @RequestBody StatisticsRequest request) {
        try {
            WeatherStatistics stats = statisticsService.calculateStatistics(
                    request.getLocation(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getMetrics()
            );
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error calculating statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}