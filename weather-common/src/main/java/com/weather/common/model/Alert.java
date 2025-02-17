package com.weather.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Alert {
    private String id;
    private boolean active;
    private List<Condition> conditions;
    private String combinator; // "AND" or "OR"
    private LocalDateTime created;
}