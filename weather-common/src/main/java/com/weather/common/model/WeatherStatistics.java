package com.weather.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherStatistics {
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private double averageTemperature;
    private double averagePrecipitation;
    private double averageWindSpeed;
    private double averageHumidity;
    private LocalDateTime calculated;
}
