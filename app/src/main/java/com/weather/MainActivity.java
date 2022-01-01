package com.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.weather.data.Location;
import com.weather.data.MainActivityViewModel;
import com.weather.data.WeatherForecast;
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

    private MainActivityViewModel mMainActivityViewModel;

    private ActivityMainBinding binding;
    private ListView WeeklyForecastList;
    private ArrayAdapter<WeatherForecast> WeeklyForecastListAdapter;

    private LocationManager mLocationManager;

    private Snackbar loadingGPSLocationDataProgressSnackbar;

    @Override
    protected void onStart() {
        super.onStart();
        mMainActivityViewModel.updateLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mMainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        mMainActivityViewModel.getAllLocations().observe(this, locations -> {
            if (locations != null) {
                Log.d(TAG, "number of locations " + locations.size());
            } else {
                Log.d(TAG, "no locations");
            }
            MainActivity.this.locations = locations;
        });

        mMainActivityViewModel.getLocation().observe(this, _location -> {
            if (_location != null) {
                Log.d(TAG, "saved location id " + _location.id);
            } else {
                Log.d(TAG, "no saved location");
            }
            location = _location;
            binding.setLocation(location);
            binding.invalidateAll();
        });

        mMainActivityViewModel.getDailyWeatherForecast().observe(this, _daily -> {
            if (_daily != null) {
                Log.d(TAG, "daily forecast available " + _daily.id);
            } else {
                Log.d(TAG, "no daily forecast");
            }
            daily = _daily;
            binding.setDaily(daily);
            binding.invalidateAll();
        });

        mMainActivityViewModel.getWeeklyWeatherForecast().observe(this, _weekly -> {
            if (_weekly != null) {
                Log.d(TAG, "weekly forecast available " + _weekly.size());
            } else {
                Log.d(TAG, "no weekly forecast");
            }
            weekly = _weekly;
            binding.setWeekly(weekly);
            binding.invalidateAll();
            WeeklyForecastListAdapter.notifyDataSetChanged();
            WeeklyForecastList.invalidate();
        });

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLocation(null);
        binding.setDaily(null);
        binding.setWeekly(null);

        WeeklyForecastList = findViewById(R.id.WeeklyForecastList);
        WeeklyForecastListAdapter = new ArrayAdapter<WeatherForecast>(this, R.layout.weekly_forecast_list_item) {

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
        };
        WeeklyForecastList.setAdapter(WeeklyForecastListAdapter);

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        loadingGPSLocationDataProgressSnackbar = Snackbar.make(findViewById(android.R.id.content), "wait while finding your location....", Snackbar.LENGTH_INDEFINITE);
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
        int i = 0;
        for (Location l : locations) {
            if (l.id == id) {
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

    private ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            attemptToGetGPSLocation();
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                            attemptToGetGPSLocation();
                        } else {
                            // No location access granted.
                        }
                    }
            );

    private static final long MAX_DISTANCE_FROM_LOCATION = 50 * 1000;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull android.location.Location location) {
            // find the location that is closest to me and change the current location to it
            float smallestDistance = Float.POSITIVE_INFINITY, foundDistance;
            Location smallestDistanceLocation = null;
            float[] distance = new float[1];
            for(Location l : locations) {
                android.location.Location.distanceBetween(l.lat, l.lon, location.getLatitude(), location.getLongitude(), distance);
                foundDistance = distance[0];
                if(foundDistance < smallestDistance) {
                    smallestDistance = foundDistance;
                    smallestDistanceLocation = l;
                }
            }
            loadingGPSLocationDataProgressSnackbar.dismiss();
            if(smallestDistanceLocation != null) {
                if(smallestDistance < MAX_DISTANCE_FROM_LOCATION) {
                    resetView();
                    mMainActivityViewModel.setLocationId(smallestDistanceLocation.id);
                    Toast.makeText(MainActivity.this, "location was set to \"" + smallestDistanceLocation.name + "\"", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "your location is too far from any known locations", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "no location found", Toast.LENGTH_LONG).show();
            }
            mLocationManager.removeUpdates(this);
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) { }

        @Override
        public void onProviderDisabled(@NonNull String provider) { }
    };

    private static final long LOCATION_REFRESH_TIME = 5000;
    private static final float LOCATION_REFRESH_DISTANCE = 1000;

    @SuppressLint("MissingPermission")
    private void attemptToGetGPSLocation() {
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    private void requestGPSLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }
        attemptToGetGPSLocation();
    }

    public void chooseLocation(View view) {
        if (locations == null || locations.size() == 0) {
            Toast.makeText(this, "No known locations", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder changeLocationDialog = new AlertDialog.Builder(MainActivity.this);
        changeLocationDialog.setIcon(R.drawable.ic_baseline_edit_location_alt_24);
        changeLocationDialog.setTitle("Change your location");

        changeLocationDialog.setSingleChoiceItems(Location.getAsStringArray(locations), getLocationIndexById(mMainActivityViewModel.getLocationId()), (dialog, which) -> {
            selectedLocation = which;
        });
        changeLocationDialog.setPositiveButton("OK", (dialog, which) -> {
            resetView();
            mMainActivityViewModel.setLocationId(getLocationIdByIndex(selectedLocation));
        });
        changeLocationDialog.setNeutralButton("USE GPS", (dialog, which) -> {
            loadingGPSLocationDataProgressSnackbar.show();
            requestGPSLocation();
        });
        changeLocationDialog.setNegativeButton("CANCEL", null);

        changeLocationDialog.show();
    }

    public void importData(View view) {
        Intent intent = new Intent(this, ImportActivity.class);
        startActivity(intent);
    }
}