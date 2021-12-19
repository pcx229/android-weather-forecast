package com.weather.data;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherForecastRepository {
    private WeatherForecastDatabase db;
    private LocationsDao mLocationsDao;
    private WeatherForecastDao mWeatherForecastDao;
    private SharedPreferences sharedPref;
    private XMLWeatherForecastParser webContent;
    private MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private MutableLiveData<StateData<Integer>> importDataFromTheWebResponseMutableLiveData = new MutableLiveData<>();

    public class StateData<T> {

        @Nullable
        private T data;

        @Nullable
        private Exception exception;

        public StateData(T data) {
            this.data = data;
        }

        public StateData(Exception exception) {
            this.exception = exception;
        }

        public T getData() {
            return data;
        }

        public Exception getException() {
            return exception;
        }

        public boolean isSuccess() {
            return data != null;
        }

        public boolean isError() {
            return exception != null;
        }
    }

    public WeatherForecastRepository(Application application) {
        db = WeatherForecastDatabase.getInstance(application);
        mLocationsDao = db.locationsDao();
        mWeatherForecastDao = db.weatherForecastDao();
        sharedPref = application.getSharedPreferences("WeatherForecast" , Context.MODE_PRIVATE);
        webContent = new XMLWeatherForecastParser(application);
        updateLocation();
    }


    public LiveData<Location> getLocation() {
        return mLocation;
    }

    @SuppressLint("ApplySharedPref")
    public void setLocationId(int id) {
        sharedPref.edit().putInt("selectedLocationId", id).commit();
        updateLocation();
    }

    public int getLocationId() {
        return sharedPref.getInt("selectedLocationId", -1);
    }

    @SuppressLint("ApplySharedPref")
    private void clearLocation() {
        sharedPref.edit().remove("selectedLocationId").commit();
        mLocation.postValue(null);
    }

    public void updateLocation() {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            mLocation.postValue(mLocationsDao._getById(getLocationId()));
        });
    }

    public LiveData<List<Location>> getAllLocations() {
        return mLocationsDao.getAll();
    }

    public LiveData<Location> getLocationById(int id) {
        return mLocationsDao.getById(id);
    }

    public void insertLocations(Location... locations) {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            mLocationsDao.insert(locations);
        });
    }

    public void updateLocations(Location... locations) {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            mLocationsDao.update(locations);
        });
    }

    public void deleteLocations(Location... locations) {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            mLocationsDao.delete(locations);
            updateLocation();
        });
    }

    public LiveData<WeatherForecast> getDailyWeatherForecast() {
        return Transformations.switchMap(mLocation, location -> location != null ? mWeatherForecastDao.getDaily(location.id) : null);
    }

    public LiveData<List<WeatherForecast>> getWeeklyWeatherForecast() {
        return Transformations.switchMap(mLocation, location -> location != null ? mWeatherForecastDao.getWeekly(location.id) : null);
    }

    public LiveData<List<WeatherForecast>> getAllWeatherForecasts() {
        return mWeatherForecastDao.getAll();
    }

    public LiveData<WeatherForecast> getWeatherForecastById(int id) {
        return mWeatherForecastDao.getById(id);
    }

    public void insertWeatherForecasts(WeatherForecast... weatherForecasts) {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            mWeatherForecastDao.insert(weatherForecasts);
        });
    }

    public void updateWeatherForecasts(WeatherForecast... weatherForecasts) {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            mWeatherForecastDao.update(weatherForecasts);
        });
    }

    public void deleteWeatherForecasts(WeatherForecast... weatherForecasts) {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            mWeatherForecastDao.delete(weatherForecasts);
        });
    }

    public void importDataFromTheWeb() {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            List<XMLWeatherForecastParser.Entry> data = webContent.getWeatherInformationFromTheWeb();
            if(data != null) {
                int count = 0;
                for(XMLWeatherForecastParser.Entry i : data) {
                    mLocationsDao.insert(i.location);
                    count++;
                    for(WeatherForecast j : i.forecast) {
                        j.locationId = i.location.id;
                        mWeatherForecastDao.insert(j);
                    }
                }
                importDataFromTheWebResponseMutableLiveData.postValue(new StateData<>(count));
            } else {
                importDataFromTheWebResponseMutableLiveData.postValue(new StateData<>(new Exception("failed to import data from the web")));
            }
        });
    }

    public LiveData<StateData<Integer>> importDataFromTheWebResponse() {
        return importDataFromTheWebResponseMutableLiveData;
    }

    public void clearAllData() {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            mLocationsDao.deleteAll();
            mWeatherForecastDao.deleteAll();
        });
        clearLocation();
    }

    public void importFromDBFile(File database) {
        WeatherForecastDatabase.databaseWriteExecutor.execute(() -> {
            WeatherForecastDatabase temp_db = db.getImportInstance(database);
            List<Location> locations = temp_db.locationsDao()._getAll();
            List<WeatherForecast> weatherForecasts = temp_db.weatherForecastDao()._getAll();
            Map<Integer, List<WeatherForecast>> locationWeatherForecast = new HashMap<>();
            for(Location l : locations) {
                locationWeatherForecast.put(l.id, new ArrayList<>());
            }
            for(WeatherForecast w : weatherForecasts) {
                w.id = 0;
                locationWeatherForecast.get(w.locationId).add(w);
            }
            long[] ids;
            for(Location l : locations) {
                int last_id = l.id;
                l.id = 0;
                ids = mLocationsDao.insert(l);
                for(WeatherForecast w : locationWeatherForecast.get(last_id)) {
                    w.locationId = (int) ids[0];
                }
            }
            mWeatherForecastDao.insert(weatherForecasts.toArray(new WeatherForecast[weatherForecasts.size()]));
            temp_db.close();
            database.delete();
        });
    }
}
