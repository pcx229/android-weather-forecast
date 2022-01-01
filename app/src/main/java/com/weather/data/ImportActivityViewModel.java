package com.weather.data;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ImportActivityViewModel extends AndroidViewModel {

    private WeatherForecastRepository mRepository;

    public ImportActivityViewModel(Application application) {
        super(application);
        mRepository = new WeatherForecastRepository(application);
    }

    public LiveData<List<Location>> getAllLocations() {
        return mRepository.getAllLocations();
    }

    public LiveData<Location> getLocationById(int id) {
        return mRepository.getLocationById(id);
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

    public LiveData<WeatherForecastRepository.StateData<Integer>> importDataFromTheWebResponse() {
        return mRepository.importDataFromTheWebResponse();
    }

    public void clearAllData() {
        mRepository.clearAllData();
    }

    public void importFromDBFile(File database) {
        mRepository.importFromDBFile(database);
    }

    public void setFilterWeatherForecasts(Map<String, String> filter) {
        mRepository.setFilterWeatherForecasts(filter);
    }

    public LiveData<List<WeatherForecast>> getFilteredWeatherForecasts() {
        return mRepository.getFilteredWeatherForecasts();
    }
}