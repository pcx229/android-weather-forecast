package com.weather.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class WeatherForecast {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int locationId;
    public Date fromForecastTime, toForecastTime;
    public float temperature, maxTemperature, minTemperature;
    public float humidity, maxHumidity, minHumidity;
    public float windSpeed, windDirection, rainFall;
    public int weatherCode;

    public WeatherForecast(int id, int locationId, Date fromForecastTime, Date toForecastTime, float temperature, float maxTemperature, float minTemperature, float humidity, float maxHumidity, float minHumidity, float windSpeed, float windDirection, float rainFall, int weatherCode) {
        this.id = id;
        this.locationId = locationId;
        this.fromForecastTime = fromForecastTime;
        this.toForecastTime = toForecastTime;
        this.temperature = temperature;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.humidity = humidity;
        this.maxHumidity = maxHumidity;
        this.minHumidity = minHumidity;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.rainFall = rainFall;
        this.weatherCode = weatherCode;
    }

    @Ignore
    public WeatherForecast(int locationId, Date fromForecastTime, Date toForecastTime, float temperature, float maxTemperature, float minTemperature, float humidity, float maxHumidity, float minHumidity, float windSpeed, float windDirection, float rainFall, int weatherCode) {
        this(0, locationId, fromForecastTime, toForecastTime, temperature, maxTemperature, minTemperature, humidity, maxHumidity, minHumidity, windSpeed, windDirection, rainFall, weatherCode);
    }

    @Ignore
    public WeatherForecast() {
        this(0, null, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Ignore
    public WeatherForecast(WeatherForecast w) {
        this(0, new Date(w.fromForecastTime.getTime()), new Date(w.toForecastTime.getTime()), w.temperature, w.maxTemperature, w.minTemperature, w.humidity, w.maxHumidity, w.minHumidity, w.windSpeed, w.windDirection, w.rainFall, w.weatherCode);
    }
}
