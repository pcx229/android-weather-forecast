package com.weather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.weather.data.WeatherForecast;
import com.weather.data.ImportActivityViewModel;
import com.weather.databinding.FragmentWeatherForecastItemBinding;

import java.util.List;

public class WeatherForecastFragment extends Fragment {

    public static final String TAG = "WEATHER_FORECAST";

    private List<WeatherForecast> weatherForecasts;
    private ListView weatherForecastListView;
    private ArrayAdapter<WeatherForecast> weatherForecastListAdapter;

    private ImportActivityViewModel mImportActivityViewModel;

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
        weatherForecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                Intent intent = new Intent(getActivity(), EditWeatherForecast.class);
                intent.putExtra("id", weatherForecasts.get(position).id);
                startActivity(intent);
            }
        });

        mImportActivityViewModel.getAllWeatherForecasts().observe(getActivity(), weatherForecasts -> {
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

        return view;
    }
}