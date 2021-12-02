package com.weather;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.weather.data.WeatherForecastViewModel;

public class ImportActivity extends AppCompatActivity {

    public static final String TAG = "IMPORTS";

    @StringRes
    final int[] TAB_TITLES = new int[]{R.string.tab_text_locations_activity_import, R.string.tab_text_weather_forecast_activity_import};

    private WeatherForecastViewModel mWeatherForecastViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeatherForecastViewModel = new ViewModelProvider(this).get(WeatherForecastViewModel.class);

        setContentView(R.layout.activity_import);
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {

            public int[] getTabTitles() {
                return TAB_TITLES;
            }

            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        Log.d(TAG, "build location fragment");
                        return LocationsFragment.newInstance();
                    case 1:
                        Log.d(TAG, "build weather forecast fragment");
                        return WeatherForecastFragment.newInstance();
                }
                return null;
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });
        TabLayout tabs = findViewById(R.id.tabs);
        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText(TAB_TITLES[position])
        ).attach();
    }

    public void addLocation(View view) {
        Intent intent = new Intent(this, EditLocation.class);
        startActivity(intent);
    }

    public void addWeatherForecast(View view) {
        Intent intent = new Intent(this, EditWeatherForecast.class);
        startActivity(intent);
    }

    public void importFromWeb(View view) {
        Snackbar.make(findViewById(android.R.id.content), "wait while loading weather forecast data from the web....", Snackbar.LENGTH_LONG).show();
        mWeatherForecastViewModel.importDataFromTheWeb();
    }

    public void deleteAll(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Data")
                .setMessage("Are you sure you want to delete all data entries(both locations and forecasts)?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mWeatherForecastViewModel.clearAllData();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}