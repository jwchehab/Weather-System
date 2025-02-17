package com.weather.storage.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.weather.common.model.Alert;
import com.weather.common.model.AlertNotification;
import com.weather.common.model.WeatherReport;
import com.weather.common.model.WeatherStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class LocalStorageService {
    private final ObjectMapper mapper;
    private final Path baseStoragePath;
    private static final String REPORTS_DIR = "reports";
    private static final String ALERTS_DIR = "alerts";
    private static final String STATISTICS_DIR = "statistics";
    private static final String NOTIFICATIONS_DIR = "notifications";

    public LocalStorageService() {
        // Configure ObjectMapper for proper serialization
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())            // For LocalDateTime handling
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)  // For date formatting
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) // More resilient to changes
                .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)    // Preserve timezone info
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);         // Skip null fields

        // Set up storage in user's documents folder
        String userHome = System.getProperty("user.home");
        baseStoragePath = Paths.get(userHome, "Documents", "WeatherApp");
        initializeStorage();
    }


    private void initializeStorage() {
        try {
            // Create main directories if they don't exist
            Files.createDirectories(baseStoragePath.resolve(REPORTS_DIR));
            Files.createDirectories(baseStoragePath.resolve(ALERTS_DIR));
            Files.createDirectories(baseStoragePath.resolve(STATISTICS_DIR));
            Files.createDirectories(baseStoragePath.resolve(NOTIFICATIONS_DIR));
            log.info("Storage directories initialized at: {}", baseStoragePath);
        } catch (Exception e) {
            log.error("Failed to initialize storage directories", e);
            throw new RuntimeException("Storage initialization failed", e);
        }
    }

    // Report Storage Methods
    public void saveWeatherReport(String location, LocalDate date, WeatherReport report) {
        try {
            String fileName = generateReportFileName(location, date);
            Path filePath = baseStoragePath.resolve(REPORTS_DIR).resolve(fileName);
            mapper.writeValue(filePath.toFile(), report);
        } catch (Exception e) {
            log.error("Failed to save weather report", e);
            throw new RuntimeException("Save operation failed", e);
        }
    }

    public Optional<WeatherReport> getWeatherReport(String location, LocalDate date) {
        try {
            String fileName = generateReportFileName(location, date);
            Path filePath = baseStoragePath.resolve(REPORTS_DIR).resolve(fileName);

            if (Files.exists(filePath)) {
                return Optional.of(mapper.readValue(filePath.toFile(), WeatherReport.class));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to read weather report", e);
            return Optional.empty();
        }
    }

    public List<WeatherReport> getWeeklyReports(String location, LocalDate startDate) {
        List<WeatherReport> reports = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            getWeatherReport(location, date).ifPresent(reports::add);
        }
        return reports;
    }

    // Alert Storage Methods
    public void saveAlert(Alert alert) {
        try {
            String fileName = String.format("alert_%s.json", alert.getId());
            Path filePath = baseStoragePath.resolve(ALERTS_DIR).resolve(fileName);
            mapper.writeValue(filePath.toFile(), alert);
            log.info("Saved alert with {} conditions", alert.getConditions().size());
        } catch (Exception e) {
            log.error("Failed to save alert", e);
            throw new RuntimeException("Save operation failed", e);
        }
    }

    public Optional<Alert> getAlert(String alertId) {
        try {
            String fileName = String.format("alert_%s.json", alertId);
            Path filePath = baseStoragePath.resolve(ALERTS_DIR).resolve(fileName);

            if (Files.exists(filePath)) {
                Alert alert = mapper.readValue(filePath.toFile(), Alert.class);
                log.info("Retrieved alert {} with {} conditions", alertId, alert.getConditions().size());
                return Optional.of(alert);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to read alert", e);
            return Optional.empty();
        }
    }

    public List<Alert> getActiveAlerts() {
        try {
            List<Alert> alerts = new ArrayList<>();
            Path alertsDir = baseStoragePath.resolve(ALERTS_DIR);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(alertsDir, "*.json")) {
                for (Path path : stream) {
                    Alert alert = mapper.readValue(path.toFile(), Alert.class);
                    if (alert.isActive()) {
                        alerts.add(alert);
                        log.debug("Found active alert with {} conditions", alert.getConditions().size());
                    }
                }
            }
            log.info("Retrieved {} active alerts", alerts.size());
            return alerts;
        } catch (Exception e) {
            log.error("Failed to read alerts", e);
            return Collections.emptyList();
        }
    }

    public void saveNotification(AlertNotification notification) {
        try {
            String fileName = String.format("notification_%s.json", notification.getId());
            Path filePath = baseStoragePath.resolve(NOTIFICATIONS_DIR).resolve(fileName);
            mapper.writeValue(filePath.toFile(), notification);
        } catch (Exception e) {
            log.error("Failed to save notification", e);
            throw new RuntimeException("Save operation failed", e);
        }
    }

    public List<AlertNotification> getNotifications() {
        List<AlertNotification> notifications = new ArrayList<>();
        Path notificationsDir = baseStoragePath.resolve(NOTIFICATIONS_DIR);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(notificationsDir, "*.json")) {
            for (Path path : stream) {
                AlertNotification notification = mapper.readValue(path.toFile(), AlertNotification.class);
                notifications.add(notification);
            }
        } catch (Exception e) {
            log.error("Failed to read notifications", e);
        }
        return notifications;
    }

    // Statistics Storage Methods
    public void saveStatistics(String location, LocalDate startDate, LocalDate endDate, WeatherStatistics statistics) {
        try {
            String fileName = generateStatisticsFileName(location, startDate, endDate);
            Path filePath = baseStoragePath.resolve(STATISTICS_DIR).resolve(fileName);
            mapper.writeValue(filePath.toFile(), statistics);
        } catch (Exception e) {
            log.error("Failed to save statistics", e);
            throw new RuntimeException("Save operation failed", e);
        }
    }

    public Optional<WeatherStatistics> getStatistics(String location, LocalDate startDate, LocalDate endDate) {
        try {
            String fileName = generateStatisticsFileName(location, startDate, endDate);
            Path filePath = baseStoragePath.resolve(STATISTICS_DIR).resolve(fileName);

            if (Files.exists(filePath)) {
                return Optional.of(mapper.readValue(filePath.toFile(), WeatherStatistics.class));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to read statistics", e);
            return Optional.empty();
        }
    }

    // Helper Methods
    private String generateReportFileName(String location, LocalDate date) {
        return String.format("%s_%s.json",
                location.toLowerCase().replace(" ", "_"),
                date.format(DateTimeFormatter.ISO_DATE));
    }

    private String generateStatisticsFileName(String location, LocalDate startDate, LocalDate endDate) {
        return String.format("stats_%s_%s_to_%s.json",
                location.toLowerCase().replace(" ", "_"),
                startDate.format(DateTimeFormatter.ISO_DATE),
                endDate.format(DateTimeFormatter.ISO_DATE));
    }

    // Cache Management
    public double getCurrentCacheSize() {
        try {
            System.out.println("Checking cache size in: " + baseStoragePath.toAbsolutePath());

            if (!Files.exists(baseStoragePath) || !Files.isDirectory(baseStoragePath)) {
                System.out.println("Cache directory does not exist: " + baseStoragePath);
                return 0.0;
            }

            long size = Files.walk(baseStoragePath)
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            long fileSize = Files.size(p);
                            System.out.println("Found file: " + p + " | Size: " + fileSize + " bytes");
                            return fileSize;
                        } catch (Exception e) {
                            System.err.println("Error getting size of file: " + p + " | " + e.getMessage());
                            return 0L;
                        }
                    })
                    .sum();

            System.out.println("Total cache size in bytes: " + size);
            double sizeInMB = size / (1024.0 * 1024.0);
            System.out.println("Total cache size: " + String.format("%.3f", sizeInMB) + " MB");

            return Double.parseDouble(String.format("%.3f", sizeInMB));
        } catch (Exception e) {
            System.err.println("Failed to calculate cache size");
            e.printStackTrace();
            return 0.0;
        }
    }



    public void clearCache() {
        try {
            Files.walk(baseStoragePath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (Exception e) {
                            log.error("Failed to delete file: {}", file, e);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
            throw new RuntimeException("Cache clear operation failed", e);
        }
    }
}