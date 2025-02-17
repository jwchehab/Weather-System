package com.weather.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertNotification {
    private String id;
    private String alertId;
    private String message;
    private LocalDateTime timestamp;
    private boolean acknowledged;
}
