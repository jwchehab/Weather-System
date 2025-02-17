package com.weather.alert.model;

import com.weather.common.model.Condition;
import lombok.Data;
import java.util.List;

@Data
public class AlertRequest {
    private List<Condition> conditions;
    private String combinator;
    private Location location;
}