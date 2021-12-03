package com.weather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.weather.data.Location;
import com.weather.data.WeatherForecast;
import com.weather.data.WeatherForecastViewModel;
import com.weather.databinding.ActivityMainBinding;
import com.weather.databinding.WeeklyForecastListItemBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "FRONT";

    private Location location;
    private WeatherForecast daily;
    private List<WeatherForecast> weekly;

    private List<Location> locations;

    private int selectedLocation;

    private WeatherForecastViewModel mWeatherForecastViewModel;

    private ActivityMainBinding binding;
    private ListView WeeklyForecastList;

    @Override
    protected void onStart() {
        super.onStart();
        mWeatherForecastViewModel.updateLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mWeatherForecastViewModel = new ViewModelProvider(this).get(WeatherForecastViewModel.class);

        mWeatherForecastViewModel.getAllLocations().observe(this, locations -> {
            if(locations != null) {
                Log.d(TAG, "number of locations " + locations.size());
            } else {
                Log.d(TAG, "no locations");
            }
            MainActivity.this.locations = locations;
        });

        mWeatherForecastViewModel.getLocation().observe(this, _location -> {
            if(_location != null) {
                Log.d(TAG, "saved location id " + _location.id);
            } else {
                Log.d(TAG, "no saved location");
            }
            location = _location;
            binding.setLocation(location);
            binding.invalidateAll();
        });

        mWeatherForecastViewModel.getDailyWeatherForecast().observe(this, _daily -> {
            if(_daily != null) {
                Log.d(TAG, "daily forecast available " + _daily.id);
            } else {
                Log.d(TAG, "no daily forecast");
            }
            daily = _daily;
            binding.setDaily(daily);
            binding.invalidateAll();
        });

        mWeatherForecastViewModel.getWeeklyWeatherForecast().observe(this, _weekly -> {
            if(_weekly != null) {
                Log.d(TAG, "weekly forecast available " + _weekly.size());
            } else {
                Log.d(TAG, "no weekly forecast");
            }
            weekly = _weekly;
            binding.setWeekly(weekly);
            binding.invalidateAll();
            WeeklyForecastList.invalidate();
        });

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLocation(null);
        binding.setDaily(null);
        binding.setWeekly(null);

        WeeklyForecastList = findViewById(R.id.WeeklyForecastList);
        WeeklyForecastList.setAdapter(new ArrayAdapter<WeatherForecast>(this, R.layout.weekly_forecast_list_item) {

            @Override
            public int getCount() {
                return weekly != null ? weekly.size() : 0;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Nullable
            @Override
            public WeatherForecast getItem(int position) {
                return weekly.get(position);
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                WeeklyForecastListItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.weekly_forecast_list_item, parent, false);
                binding.setWeekDay(getItem(position));
                return binding.getRoot();
            }
        });
    }

    private void resetView() {
        location = null;
        daily = null;
        weekly = null;
        binding.setLocation(null);
        binding.setDaily(null);
        binding.setWeekly(null);
        binding.invalidateAll();
        WeeklyForecastList.invalidate();
    }

    private int getLocationIndexById(int id) {
        int index = -1;
        int i=0;
        for(Location l : locations) {
            if(l.id == id) {
                index = i;
                break;
            }
            i++;
        }
        return index;
    }

    private int getLocationIdByIndex(int index) {
        return locations.get(index).id;
    }

    public void chooseLocation(View view) {
        if(locations == null || locations.size() == 0) {
            Toast.makeText(this, "No known locations", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder changeLocationDialog = new AlertDialog.Builder(MainActivity.this);
        changeLocationDialog.setIcon(R.drawable.ic_baseline_edit_location_alt_24);
        changeLocationDialog.setTitle("Change your location");

        changeLocationDialog.setSingleChoiceItems(Location.getAsStringArray(locations), getLocationIndexById(mWeatherForecastViewModel.getLocationId()), (dialog, which) -> {
            selectedLocation = which;
        });
        changeLocationDialog.setPositiveButton("OK", (dialog, which) -> {
            resetView();
            mWeatherForecastViewModel.setLocationId(getLocationIdByIndex(selectedLocation));
        });
        changeLocationDialog.setNegativeButton("Cancel", null);

        changeLocationDialog.show();
    }

    public void importData(View view) {
        Intent intent = new Intent(this, ImportActivity.class);
        startActivity(intent);
    }
}