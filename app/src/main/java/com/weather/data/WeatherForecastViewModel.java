package com.weather.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class WeatherForecastViewModel extends AndroidViewModel {

    private WeatherForecastRepository mRepository;

    public WeatherForecastViewModel(Application application) {
        super(application);
        mRepository = new WeatherForecastRepository(application);
    }

    public LiveData<List<Location>> getAllLocations() {
        return mRepository.getAllLocations();
    }

    public LiveData<Location> getLocationById(int id) {
        return mRepository.getLocationById(id);
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

    public void insertLocations(Location... locations) {
        mRepository.insertLocations(locations);
    }

    public void updateLocations(Location... locations) {
        mRepository.updateLocations(locations);
    }

    public void deleteLocations(Location... locations) {
        mRepository.deleteLocations(locations);
    }

    public LiveData<WeatherForecast> getDailyWeatherForecast() {
        return mRepository.getDailyWeatherForecast();
    }

    public LiveData<List<WeatherForecast>> getWeeklyWeatherForecast() {
        return mRepository.getWeeklyWeatherForecast();
    }

    public LiveData<List<WeatherForecast>> getAllWeatherForecasts() {
        return mRepository.getAllWeatherForecasts();
    }

    public LiveData<WeatherForecast> getWeatherForecastById(int id) {
        return mRepository.getWeatherForecastById(id);
    }

    public void insertWeatherForecasts(WeatherForecast... weatherForecasts) {
        mRepository.insertWeatherForecasts(weatherForecasts);
    }

    public void updateWeatherForecasts(WeatherForecast... weatherForecasts) {
        mRepository.updateWeatherForecasts(weatherForecasts);
    }

    public void deleteWeatherForecasts(WeatherForecast... weatherForecasts) {
        mRepository.deleteWeatherForecasts(weatherForecasts);
    }

    public void importDataFromTheWeb() {
        mRepository.importDataFromTheWeb();
    }

    public void clearAllData() {
        mRepository.clearAllData();
    }
}