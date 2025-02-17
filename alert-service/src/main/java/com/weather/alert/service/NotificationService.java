package com.weather.alert.service;

import com.weather.common.model.AlertNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendWebSocketNotification(AlertNotification notification) {
        log.info("Sending WebSocket notification: {}", notification.getMessage());
        messagingTemplate.convertAndSend("/topic/alerts", notification);
    }
}
