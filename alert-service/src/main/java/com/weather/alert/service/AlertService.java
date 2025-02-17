package com.weather.alert.service;

import com.weather.common.model.Alert;
import com.weather.common.model.AlertNotification;
import com.weather.common.model.Condition;
import com.weather.common.model.WeatherReport;
import com.weather.storage.service.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {
    private final RestTemplate restTemplate;
    private final LocalStorageService storageService;

    public Alert createAlert(List<Condition> conditions, String combinator) {
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setConditions(conditions);
        alert.setCombinator(combinator);
        alert.setActive(true);
        alert.setCreated(LocalDateTime.now());

        storageService.saveAlert(alert);
        return alert;
    }

    public List<Alert> getActiveAlerts() {
        return storageService.getActiveAlerts();
    }

    public void updateAlert(String alertId, boolean active) {
        Optional<Alert> alert = storageService.getAlert(alertId);
        if (alert.isPresent()) {
            Alert updatedAlert = alert.get();
            updatedAlert.setActive(active);
            storageService.saveAlert(updatedAlert);
        }
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkAlertConditions() {
        List<Alert> activeAlerts = getActiveAlerts();

        for (Alert alert : activeAlerts) {
            try {
                WeatherReport currentWeather = getCurrentWeather();
                if (currentWeather != null) {
                    checkConditions(alert, currentWeather);
                }
            } catch (Exception e) {
                log.error("Failed to check alert condition", e);
            }
        }
    }

    private WeatherReport getCurrentWeather() {
        try {
            String url = "http://localhost:8081/api/weather/current";
            return restTemplate.getForObject(url, WeatherReport.class);
        } catch (Exception e) {
            log.error("Failed to fetch current weather", e);
            return null;
        }
    }

    private void checkConditions(Alert alert, WeatherReport weather) {
        List<Boolean> conditionResults = alert.getConditions().stream()
                .map(condition -> checkSingleCondition(condition, weather))
                .collect(Collectors.toList());

        boolean triggered = "AND".equals(alert.getCombinator())
                ? conditionResults.stream().allMatch(result -> result)
                : conditionResults.stream().anyMatch(result -> result);

        if (triggered) {
            createNotification(alert, weather);
        }
    }

    private boolean checkSingleCondition(Condition condition, WeatherReport weather) {
        double value = switch (condition.getParameter().toLowerCase()) {
            case "temperature" -> weather.getHighTemp();
            case "precipitation" -> weather.getPrecipitationChance();
            case "wind" -> weather.getWindSpeed();
            case "humidity" -> weather.getHumidity();
            default -> 0.0;
        };

        return switch (condition.getOperator()) {
            case ">" -> value > condition.getThreshold();
            case "<" -> value < condition.getThreshold();
            case "=" -> Math.abs(value - condition.getThreshold()) < 0.01;
            default -> false;
        };
    }

    private final NotificationService notificationService;

    private void createNotification(Alert alert, WeatherReport weather) {
        AlertNotification notification = AlertNotification.builder()
                .id(UUID.randomUUID().toString())
                .alertId(alert.getId())
                .message(formatAlertMessage(alert, weather))
                .timestamp(LocalDateTime.now())
                .acknowledged(false)
                .build();

        storageService.saveNotification(notification);

        notificationService.sendWebSocketNotification(notification);
    }

    private String formatAlertMessage(Alert alert, WeatherReport weather) {
        StringBuilder message = new StringBuilder("Weather Alert: ");
        for (int i = 0; i < alert.getConditions().size(); i++) {
            Condition condition = alert.getConditions().get(i);
            if (i > 0) {
                message.append(" ").append(alert.getCombinator()).append(" ");
            }
            double currentValue = switch (condition.getParameter().toLowerCase()) {
                case "temperature" -> weather.getHighTemp();
                case "precipitation" -> weather.getPrecipitationChance();
                case "wind" -> weather.getWindSpeed();
                case "humidity" -> weather.getHumidity();
                default -> 0.0;
            };
            message.append(String.format("%s %s %.1f (Current value: %.1f)",
                    condition.getParameter(),
                    condition.getOperator(),
                    condition.getThreshold(),
                    currentValue));
        }
        return message.toString();
    }
}