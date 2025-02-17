package com.weather.storage.controller;

import com.weather.storage.service.LocalStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {
    private final LocalStorageService storageService;

    @GetMapping("/size")
    public ResponseEntity<Double> getCacheSize() {
        try {
            double size = storageService.getCurrentCacheSize();
            return ResponseEntity.ok(size);
        } catch (Exception e) {
            log.error("Error getting cache size", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clearCache() {
        try {
            storageService.clearCache();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error clearing cache", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}