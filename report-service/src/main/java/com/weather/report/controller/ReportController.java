package com.weather.report.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.util.List;
import com.weather.common.model.WeatherReport;
import com.weather.report.service.WeatherReportService;

@RestController
@RequestMapping("/api/weather")
@Slf4j
@RequiredArgsConstructor
public class ReportController {
    private final WeatherReportService reportService;

    @GetMapping("/report")
    public ResponseEntity<WeatherReport> getWeatherReport(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            WeatherReport report = reportService.getWeatherReport(location, date);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error fetching weather report", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/forecast")
    public ResponseEntity<List<WeatherReport>> getWeeklyForecast(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        try {
            List<WeatherReport> forecast = reportService.getWeeklyReport(location, startDate);
            return ResponseEntity.ok(forecast);
        } catch (Exception e) {
            log.error("Error fetching forecast", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}