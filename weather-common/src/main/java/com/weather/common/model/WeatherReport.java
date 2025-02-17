package com.weather.common.model;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WeatherReport {
    private String location;
    private LocalDate date;
    private double highTemp;
    private double lowTemp;
    private double humidity;
    private double windSpeed;
    private String windDirection;
    private double precipitationChance;
}