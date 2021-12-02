package com.weather;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.weather.data.Location;
import com.weather.data.WeatherForecast;
import com.weather.data.WeatherForecastViewModel;
import com.weather.databinding.ActivityEditLocationBinding;
import com.weather.databinding.ActivityEditWeatherForecastBinding;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditWeatherForecast extends AppCompatActivity {

    private WeatherForecast weatherForecast;

    private WeatherForecastViewModel mWeatherForecastViewModel;

    private ActivityEditWeatherForecastBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_weather_forecast);

        mWeatherForecastViewModel = new ViewModelProvider(this).get(WeatherForecastViewModel.class);

        int id = -1;
        Intent intent = getIntent();
        if(intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else if(savedInstanceState != null && savedInstanceState.containsKey("id")) {
            id = savedInstanceState.getInt("id", -1);
        }
        if(id != -1) {
            mWeatherForecastViewModel.getWeatherForecastById(id).observe(this, _weatherForecast -> {
                if(_weatherForecast != null) {
                    weatherForecast = _weatherForecast;
                    binding.setWeatherForecast(weatherForecast);
                    binding.invalidateAll();
                }
            });
        } else {
            weatherForecast = new WeatherForecast();
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_weather_forecast);
        binding.setWeatherForecast(weatherForecast);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(weatherForecast != null && weatherForecast.id != -1) {
            savedInstanceState.putInt("id", weatherForecast.id);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public void save(View view) {
        mWeatherForecastViewModel.insertWeatherForecasts(weatherForecast);
        finish();
    }

    public void update(View view) {
        mWeatherForecastViewModel.updateWeatherForecasts(weatherForecast);
        finish();
    }

    public void delete(View view) {
        mWeatherForecastViewModel.deleteWeatherForecasts(weatherForecast);
        finish();
    }
}