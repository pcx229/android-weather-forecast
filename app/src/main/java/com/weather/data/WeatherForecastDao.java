package com.weather.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface WeatherForecastDao {
    @Query("SELECT * FROM weatherforecast ORDER BY fromForecastTime ASC")
    LiveData<List<WeatherForecast>> getAll();

    @Query("SELECT * FROM weatherforecast WHERE LocationId LIKE :locationId AND toForecastTime > :after AND fromForecastTime < :before ORDER BY fromForecastTime ASC")
    LiveData<List<WeatherForecast>> getFiltered(String locationId, String after, String before);

    @Query("SELECT * FROM weatherforecast ORDER BY fromForecastTime ASC")
    List<WeatherForecast> _getAll();

    @Query("SELECT * FROM weatherforecast WHERE id IN (:weatherForecastIds)")
    LiveData<List<WeatherForecast>> getByIds(int... weatherForecastIds);

    @Query("SELECT * FROM weatherforecast WHERE id = (:id)")
    LiveData<WeatherForecast> getById(int id);

    @Query("SELECT * FROM weatherforecast WHERE locationId = (:locationId)")
    LiveData<List<WeatherForecast>> getByLocationId(int locationId);

    // for current time forecast get the shortest forecast that was predicted closest to my time
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, (toForecastTime-fromForecastTime) AS dur, JULIANDAY(:now/1000, 'unixepoch')-JULIANDAY(fromForecastTime/1000, 'unixepoch') AS diff " +
            "FROM weatherforecast " +
            "WHERE locationId = (:locationId) AND diff > -1 AND diff < 1 " +
            "ORDER BY ABS(diff) ASC, dur ASC LIMIT 1")
    LiveData<WeatherForecast> getDaily(Date now, int locationId);

    // for daily forecast get the longest forecast that was predicted at my date
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, (toForecastTime-fromForecastTime) AS dur, JULIANDAY(:now/1000, 'unixepoch')-JULIANDAY(fromForecastTime/1000, 'unixepoch') AS diff " +
            "FROM weatherforecast " +
            "WHERE locationId = (:locationId) AND diff <= 0 AND diff > -7 " +
            "GROUP BY CAST(diff AS INTEGER) HAVING MAX(dur) ORDER BY diff DESC")
    LiveData<List<WeatherForecast>> getWeekly(Date now, int locationId);

    @Insert
    long[] insert(WeatherForecast... weatherForecasts);

    @Update
    void update(WeatherForecast... weatherForecasts);

    @Delete
    void delete(WeatherForecast... weatherForecasts);

    @Query("DELETE FROM weatherforecast")
    void deleteAll();
}