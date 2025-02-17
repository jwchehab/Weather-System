package com.weather.statistics.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class StatisticsRequest {
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<String> metrics;
}