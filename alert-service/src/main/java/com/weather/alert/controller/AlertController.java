package com.weather.alert.controller;

import com.weather.common.model.Alert;
import com.weather.alert.model.AlertRequest;
import com.weather.alert.service.AlertService;
import com.weather.storage.service.LocalStorageService;
import com.weather.common.model.AlertNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Slf4j
@RequiredArgsConstructor
public class AlertController {
    private final AlertService alertService;
    private final LocalStorageService storageService;

    @PostMapping
    public ResponseEntity<Alert> createAlert(@RequestBody AlertRequest request) {
        try {
            Alert alert = alertService.createAlert(
                    request.getConditions(),
                    request.getCombinator()
            );
            return ResponseEntity.ok(alert);
        } catch (Exception e) {
            log.error("Error creating alert", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        try {
            List<Alert> alerts = alertService.getActiveAlerts();
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            log.error("Error fetching alerts", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{alertId}/status")
    public ResponseEntity<Void> updateAlertStatus(
            @PathVariable("alertId") String alertId,
            @RequestParam("active") boolean active) {
        try {
            alertService.updateAlert(alertId, active);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating alert status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<AlertNotification>> getNotifications() {
        try {
            List<AlertNotification> notifications = storageService.getNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            log.error("Error fetching notifications", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}