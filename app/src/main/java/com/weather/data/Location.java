package com.weather.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Location {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public float lat, lon;
    public float height;

    public Location(int id, String name, float lat, float lon, float height) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.height = height;
    }

    @Ignore
    public Location(String name, float lat, float lon, float height) {
        this(0, name, lat, lon, height);
    }

    @Ignore
    public Location() {
        this(0, "", 0, 0, 0);
    }

    @Ignore
    public static String[] getAsStringArray(List<Location> locations) {
        List<String> locationsNames = new ArrayList<>();
        for(Location l : locations) {
            locationsNames.add(l.name);
        }
        return locationsNames.toArray(new String[locationsNames.size()]);
    }
}