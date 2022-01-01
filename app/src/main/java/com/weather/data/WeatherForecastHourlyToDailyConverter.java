package com.weather.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherForecastHourlyToDailyConverter {

    private static class Daily {
        public int locationId;
        public Date fromForecastTime, toForecastTime;
        public float maxTemperature, minTemperature;
        public float maxHumidity, minHumidity;
        private float sumWindSpeed, sumTemperature, sumHumidity;
        private int countWindSpeed, countTemperature, countHumidity;
        public float rainFall;
        public int weatherCode;
        private float scoreWeatherCode;
        // good times are between 12:00 20:00
        private Date goodTimesStart, goodTimesEnd;

        public Daily(int locationId, Date fromForecastTime, float temperature, float maxTemperature, float minTemperature, float humidity, float maxHumidity, float minHumidity, float windSpeed, float rainFall, int weatherCode) {
            this.locationId = locationId;
            this.fromForecastTime = fromForecastTime;
            this.sumTemperature = temperature;
            this.countTemperature = 1;
            this.maxTemperature = maxTemperature;
            this.minTemperature = minTemperature;
            this.sumHumidity = humidity;
            this.countHumidity = 1;
            this.maxHumidity = maxHumidity;
            this.minHumidity = minHumidity;
            this.sumWindSpeed = windSpeed;
            this.rainFall = rainFall;
            this.countWindSpeed = 1;
            this.weatherCode = weatherCode;
            // set time full day
            Calendar saved = Calendar.getInstance();
            saved.setTime(fromForecastTime);
            saved.set(saved.get(Calendar.YEAR), saved.get(Calendar.MONTH), saved.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            this.fromForecastTime = saved.getTime();
            saved.add(Calendar.HOUR_OF_DAY, 24);
            this.toForecastTime = saved.getTime();
            // set good times and update weather code
            saved.setTime(fromForecastTime);
            saved.set(saved.get(Calendar.YEAR), saved.get(Calendar.MONTH), saved.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            saved.set(Calendar.HOUR_OF_DAY, 12);
            goodTimesStart = saved.getTime();
            saved.set(Calendar.HOUR_OF_DAY, 20);
            goodTimesEnd = saved.getTime();
            updateHourWeatherCode(weatherCode, fromForecastTime, toForecastTime);
        }

        public void updateHourWeatherCode(int weatherCode, Date fromForecastTime, Date toForecastTime) {
            float scoreWeatherCode = 0;
            long a1 = goodTimesStart.getTime(),
                    a2 = goodTimesEnd.getTime();
            long b1 = fromForecastTime.getTime(),
                    b2 = toForecastTime.getTime();
            if(a1 <= b1 && b1 <= a2) {
                if(a1 <= b2 && b2 <= a2) {
                    scoreWeatherCode = b2-b1;
                } else {
                    scoreWeatherCode = a2-b1;
                }
            } else if(a1 <= b2 && b2 <= a2) {
                scoreWeatherCode = b2-a1;
            }
            if(this.weatherCode == 0 || this.scoreWeatherCode < scoreWeatherCode) {
                this.weatherCode = weatherCode;
                this.scoreWeatherCode = scoreWeatherCode;
            }
        }

        public void updateTemperature(float temperature, float maxTemperature, float minTemperature) {
            if (this.minTemperature > minTemperature) {
                this.minTemperature = minTemperature;
            }
            if (this.maxTemperature < maxTemperature) {
                this.maxTemperature = maxTemperature;
            }
            sumTemperature += temperature;
            countTemperature += 1;
        }

        public void updateHumidity(float humidity, float maxHumidity, float minHumidity) {
            if (this.minHumidity > minHumidity) {
                this.minHumidity = minHumidity;
            }
            if (this.maxHumidity < maxHumidity) {
                this.maxHumidity = maxHumidity;
            }
            sumHumidity += humidity;
            countHumidity += 1;
        }

        public void updateRainFall(float rainFall) {
            this.rainFall += rainFall;
        }

        public void updateWindSpeed(float windSpeed) {
            this.sumWindSpeed += windSpeed;
            this.countWindSpeed += 1;
        }

        public float avgWindSpeed() {
            return 1.f * sumWindSpeed / countWindSpeed;
        }

        public float avgTemperature() {
            return 1.f * sumTemperature / countTemperature;
        }

        public float avgHumidity() {
            return 1.f * sumHumidity / countHumidity;
        }
    }

    public static List<WeatherForecast> getDailyForecastFromHourly(List<WeatherForecast> weatherForecasts) {
        Map<String, Daily> mdf = new HashMap<>();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        for(WeatherForecast w : weatherForecasts) {
            String k = fmt.format(w.fromForecastTime);
            if (mdf.containsKey(k)) {
                Daily daily = mdf.get(k);
                daily.updateHourWeatherCode(w.weatherCode, w.fromForecastTime, w.toForecastTime);
                daily.updateTemperature(w.temperature, w.maxTemperature, w.minTemperature);
                daily.updateHumidity(w.humidity, w.maxHumidity, w.minHumidity);
                daily.updateRainFall(w.rainFall);
                daily.updateWindSpeed(w.windSpeed);
            } else {
                Daily daily = new Daily(w.locationId, w.fromForecastTime, w.temperature, w.maxTemperature, w.minTemperature, w.humidity, w.maxHumidity, w.minHumidity, w.windSpeed, w.rainFall, w.weatherCode);
                mdf.put(k, daily);
            }
        }
        List<WeatherForecast> weeklyWeatherForecasts = new ArrayList<>();
        for(Daily w : mdf.values()) {
            weeklyWeatherForecasts.add(new WeatherForecast(w.locationId, w.fromForecastTime, w.toForecastTime, w.avgTemperature(), w.maxTemperature, w.minTemperature, w.avgHumidity(), w.maxHumidity, w.minHumidity, w.avgWindSpeed(), 0, w.rainFall, w.weatherCode));
        }
        return weeklyWeatherForecasts;
    }
}
