package com.weather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.weather.data.ImportActivityViewModel;
import com.weather.data.WeatherForecast;
import com.weather.databinding.FragmentWeatherForecastItemBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherForecastFragment extends Fragment {

    public static final String TAG = "WEATHER_FORECAST";

    private ImageButton searchFilterButton;

    private List<WeatherForecast> weatherForecasts;
    private ListView weatherForecastListView;
    private ArrayAdapter<WeatherForecast> weatherForecastListAdapter;

    private ImportActivityViewModel mImportActivityViewModel;

    private WeatherForecastFilterDialog filterDialog;
    private Map<String, String> filterParams = new HashMap<>();

    public WeatherForecastFragment() {

    }

    public static WeatherForecastFragment newInstance() {
        WeatherForecastFragment fragment = new WeatherForecastFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImportActivityViewModel = new ViewModelProvider(this).get(ImportActivityViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather_forecast_list, container, false);

        weatherForecastListView = (ListView) view.findViewById(R.id.WeatherForecastListView);
        weatherForecastListAdapter = new ArrayAdapter<WeatherForecast>(getActivity(), R.layout.fragment_weather_forecast_item) {

            @Override
            public int getCount() {
                return weatherForecasts != null ? weatherForecasts.size() : 0;
            }

            @Nullable
            @Override
            public WeatherForecast getItem(int position) {
                return weatherForecasts.get(position);
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                WeatherForecast weatherForecast = getItem(position);
                LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                FragmentWeatherForecastItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_weather_forecast_item, parent, false);
                binding.setWeatherForecast(weatherForecast);
                return binding.getRoot();
            }

        };
        weatherForecastListView.setAdapter(weatherForecastListAdapter);
        weatherForecastListView.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            Intent intent = new Intent(getActivity(), EditWeatherForecast.class);
            intent.putExtra("id", weatherForecasts.get(position).id);
            startActivity(intent);
        });

        mImportActivityViewModel.getFilteredWeatherForecasts().observe(getActivity(), weatherForecasts -> {
            if(weatherForecasts != null) {
                Log.d(TAG, "number of weather forecasts " + weatherForecasts.size());
            } else {
                Log.d(TAG, "no weather forecasts");
            }
            WeatherForecastFragment.this.weatherForecasts = weatherForecasts;
            weatherForecastListAdapter.notifyDataSetChanged();
            weatherForecastListView.invalidate();
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .detach(WeatherForecastFragment.this)
                    .attach(WeatherForecastFragment.this)
                    .commitAllowingStateLoss();
            getView().invalidate();
            getView().requestLayout();
        });

        filterDialog = new WeatherForecastFilterDialog(getActivity());
        filterDialog.setOnFilterListener(filter -> {
            this.filterParams = filter;
            mImportActivityViewModel.setFilterWeatherForecasts(filter);
        });

        searchFilterButton = (ImageButton) view.findViewById(R.id.SearchFilterButton);
        searchFilterButton.setOnClickListener(v -> filterDialog.show());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mImportActivityViewModel.setFilterWeatherForecasts(filterParams);
    }
}