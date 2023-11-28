package com.example.coolweather.json;

import java.util.List;

public class Weather {
    public Basic basic;
    public Now now;
    public List<Forcast> forcastList;
    public AQI aqi;
    public Suggestion suggestion;
    public String status;
}
