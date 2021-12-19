package com.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.weather.data.WeatherForecast;
import com.weather.data.ImportActivityViewModel;
import com.weather.databinding.ActivityEditWeatherForecastBinding;

public class EditWeatherForecast extends AppCompatActivity {

    private WeatherForecast weatherForecast;

    private ImportActivityViewModel mImportActivityViewModel;

    private ActivityEditWeatherForecastBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_weather_forecast);

        mImportActivityViewModel = new ViewModelProvider(this).get(ImportActivityViewModel.class);

        int id = -1;
        Intent intent = getIntent();
        if(intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else if(savedInstanceState != null && savedInstanceState.containsKey("id")) {
            id = savedInstanceState.getInt("id", -1);
        }
        if(id != -1) {
            mImportActivityViewModel.getWeatherForecastById(id).observe(this, _weatherForecast -> {
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
        mImportActivityViewModel.insertWeatherForecasts(weatherForecast);
        finish();
    }

    public void update(View view) {
        mImportActivityViewModel.updateWeatherForecasts(weatherForecast);
        finish();
    }

    public void delete(View view) {
        mImportActivityViewModel.deleteWeatherForecasts(weatherForecast);
        finish();
    }
}