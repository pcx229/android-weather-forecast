package com.weather;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.Button;

import androidx.databinding.DataBindingUtil;

import com.weather.databinding.DialogWeatherForecastFilterBinding;

import java.util.HashMap;
import java.util.Map;

public class WeatherForecastFilterDialog extends Dialog {

    public static final String TAG = "WEATHER_FORECAST_FILTER";

    private Button applyButton, clearButton;

    private DialogWeatherForecastFilterBinding binding;
    private Map<String, String> filter = new HashMap<>();

    public WeatherForecastFilterDialog(Activity activity) {
        super(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_weather_forecast_filter, null, false);
        binding.setFilter(filter);
        setContentView(binding.getRoot());

        applyButton = (Button) findViewById(R.id.ApplyButton);
        applyButton.setOnClickListener(v -> {
            Log.d(TAG, "apply filter {locationId:" + filter.getOrDefault("locationId", "[null]") + ", after:" + filter.getOrDefault("after", "[null]") + ", before:" + filter.getOrDefault("before", "[null]") + "}");
            notifyFilterListener();
            dismiss();
        });

        clearButton = (Button) findViewById(R.id.ClearButton);
        clearButton.setOnClickListener(v -> {
            Log.d(TAG, "clear filter");
            filter.clear();
            binding.invalidateAll();
            notifyFilterListener();
            dismiss();
        });
    }

    public interface OnFilterListener {
        void onFilter(Map<String, String> filter);
    }

    private OnFilterListener filterListener;

    public void setOnFilterListener(OnFilterListener listener) {
        filterListener = listener;
    }

    public void removeOnFilterListener() {
        filterListener = null;
    }

    private void notifyFilterListener() {
        if(filterListener != null) {
            filterListener.onFilter(filter);
        }
    }
}