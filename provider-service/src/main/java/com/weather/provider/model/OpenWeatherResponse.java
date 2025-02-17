package com.weather.provider.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class OpenWeatherResponse {
    private Main main;
    private Wind wind;
    private Rain rain;
    private List<WeatherData> list;

    @Data
    public static class WeatherData {
        private Main main;
        private Wind wind;
        private Rain rain;
        @JsonProperty("dt_txt")
        private String dtTxt;
    }

    @Data
    public static class Main {
        private double temp;
        @JsonProperty("temp_min")
        private double lowTemp;
        @JsonProperty("temp_max")
        private double highTemp;
        private double humidity;
    }

    @Data
    public static class Wind {
        private double speed;
        private double deg;
    }

    @Data
    public static class Rain {
        @JsonProperty("1h")
        private double oneHour;
        @JsonProperty("3h")
        private double threeHours;
    }
}
