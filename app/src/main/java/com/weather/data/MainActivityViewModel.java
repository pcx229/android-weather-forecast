package com.weather.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.io.File;
import java.util.List;

public class MainActivityViewModel extends AndroidViewModel {

    private WeatherForecastRepository mRepository;

    public MainActivityViewModel(Application application) {
        super(application);
        mRepository = new WeatherForecastRepository(application);
    }

    public LiveData<List<Location>> getAllLocations() {
        return mRepository.getAllLocations();
    }

    public LiveData<Location> getLocation() {
        return mRepository.getLocation();
    }

    public void updateLocation() {
        mRepository.updateLocation();
    }

    public void setLocationId(int id) {
        mRepository.setLocationId(id);
    }

    public int getLocationId() {
        return mRepository.getLocationId();
    }

    public LiveData<WeatherForecast> getDailyWeatherForecast() {
        return mRepository.getDailyWeatherForecast();
    }

    public LiveData<List<WeatherForecast>> getWeeklyWeatherForecast() {
        return mRepository.getWeeklyWeatherForecast();
    }
}