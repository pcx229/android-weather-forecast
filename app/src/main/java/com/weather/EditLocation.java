package com.weather;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.weather.data.Location;
import com.weather.data.WeatherForecastViewModel;
import com.weather.databinding.ActivityEditLocationBinding;

public class EditLocation extends AppCompatActivity {

    private Location location;

    private WeatherForecastViewModel mWeatherForecastViewModel;

    private ActivityEditLocationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);

        mWeatherForecastViewModel = new ViewModelProvider(this).get(WeatherForecastViewModel.class);

        int id = -1;
        Intent intent = getIntent();
        if(intent.hasExtra("id")) {
            id = intent.getIntExtra("id", -1);
        } else if(savedInstanceState != null && savedInstanceState.containsKey("id")) {
            id = savedInstanceState.getInt("id", -1);
        }
        if(id != -1) {
            mWeatherForecastViewModel.getLocationById(id).observe(this, _location -> {
                if(_location != null) {
                    location = _location;
                    binding.setLocation(location);
                    binding.invalidateAll();
                }
            });
        } else {
            location = new Location();
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_location);
        binding.setLocation(location);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if(location != null && location.id != -1) {
            savedInstanceState.putInt("id", location.id);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public void save(View view) {
        mWeatherForecastViewModel.insertLocations(location);
        finish();
    }

    public void update(View view) {
        mWeatherForecastViewModel.updateLocations(location);
        finish();
    }

    public void delete(View view) {
        mWeatherForecastViewModel.deleteLocations(location);
        finish();
    }
}