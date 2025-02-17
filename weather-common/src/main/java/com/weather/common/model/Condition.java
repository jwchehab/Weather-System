package com.weather.common.model;

import lombok.Data;

@Data
public class Condition {
    private String parameter;
    private String operator;
    private double threshold;
}