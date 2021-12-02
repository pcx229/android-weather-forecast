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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.weather.data.Location;
import com.weather.data.WeatherForecastViewModel;
import com.weather.databinding.FragmentLocationItemBinding;
import com.weather.databinding.WeeklyForecastListItemBinding;

import java.util.List;

public class LocationsFragment extends Fragment {

    public static final String TAG = "LOCATIONS";

    private List<Location> locations;
    private ListView locationsListView;
    private ArrayAdapter<Location> locationListAdapter;

    private WeatherForecastViewModel mWeatherForecastViewModel;

    public LocationsFragment() {

    }

    public static LocationsFragment newInstance() {
        LocationsFragment fragment = new LocationsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeatherForecastViewModel = new ViewModelProvider(this).get(WeatherForecastViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locations_list, container, false);

        locationsListView = (ListView) view.findViewById(R.id.LocationsListView);
        locationListAdapter = new ArrayAdapter<Location>(getActivity(), R.layout.fragment_location_item) {

            @Override
            public int getCount() {
                return locations != null ? locations.size() : 0;
            }

            @Nullable
            @Override
            public Location getItem(int position) {
                return locations.get(position);
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                Location location = getItem(position);
                LayoutInflater layoutInflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                FragmentLocationItemBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_location_item, parent, false);
                binding.setLocation(location);
                return binding.getRoot();
            }

        };
        locationsListView.setAdapter(locationListAdapter);
        locationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                Intent intent = new Intent(getActivity(), EditLocation.class);
                intent.putExtra("id", locations.get(position).id);
                startActivity(intent);
            }
        });

        mWeatherForecastViewModel.getAllLocations().observe(getActivity(), locations -> {
            if(locations != null) {
                Log.d(TAG, "number of locations " + locations.size());
            } else {
                Log.d(TAG, "no locations");
            }
            LocationsFragment.this.locations = locations;
            locationListAdapter.notifyDataSetChanged();
            locationsListView.invalidate();
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .detach(LocationsFragment.this)
                    .attach(LocationsFragment.this)
                    .commitAllowingStateLoss();
            getView().invalidate();
            getView().requestLayout();
        });

        return view;
    }
}