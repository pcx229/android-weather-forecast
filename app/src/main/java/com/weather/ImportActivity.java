package com.weather;

import android.Manifest;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.weather.data.WeatherForecastViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

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
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete all forecasts and import from ims.gov.il the updated forecasts?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Snackbar.make(findViewById(android.R.id.content), "wait while loading weather forecast data from the web....", Snackbar.LENGTH_LONG).show();
                        mWeatherForecastViewModel.importDataFromTheWeb();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public void deleteAll(View view) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete all data entries(both locations and forecasts)?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mWeatherForecastViewModel.clearAllData();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void exportDatabase() {
        try {
            File database = new File(getDataDir(), "/databases/weather_forecast");
            Log.d(TAG, "exported database from " + database.getAbsoluteFile());
            File cash_database = new File(getCacheDir(), "weather_forecast_database.sqlite3");
            if(cash_database.exists()) {
                cash_database.delete();
            }
            Files.copy(database.toPath(), cash_database.toPath());
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", cash_database.getAbsoluteFile());
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.setType("*/*");
            share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Weather Forecast Database"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @RequiresApi(api = Build.VERSION_CODES.Q)
                @Override
                public void onActivityResult(Uri uri) {
                    if(uri != null) {
                        try {
                            Log.d(TAG, "imported database from " + uri.getPath());
                            File cash_database = new File(getDataDir(), "/databases/weather_forecast_import");
                            if(cash_database.exists()) {
                                cash_database.delete();
                            }
                            InputStream fis = getContentResolver().openInputStream(uri);
                            FileOutputStream fos = new FileOutputStream(cash_database);
                            FileUtils.copy(fis, fos);
                            fos.close();
                            fis.close();
                            mWeatherForecastViewModel.importFromDBFile(cash_database);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    public void importDatabase() {
        mGetContent.launch("*/*");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void importAndExport(View view) {
        new AlertDialog.Builder(this)
                .setMessage("Which action you wish to do?")
                .setPositiveButton("IMPORT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        importDatabase();
                    }
                })
                .setNegativeButton("EXPORT", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        exportDatabase();
                    }
                })
                .show();
    }
}