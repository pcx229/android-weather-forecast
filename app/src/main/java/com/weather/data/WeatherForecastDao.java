package com.weather.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WeatherForecastDao {
    @Query("SELECT * FROM weatherforecast ORDER BY fromForecastTime ASC")
    LiveData<List<WeatherForecast>> getAll();

    @Query("SELECT * FROM weatherforecast ORDER BY fromForecastTime ASC")
    List<WeatherForecast> _getAll();

    @Query("SELECT * FROM weatherforecast WHERE id IN (:weatherForecastIds)")
    LiveData<List<WeatherForecast>> getByIds(int... weatherForecastIds);

    @Query("SELECT * FROM weatherforecast WHERE id = (:id)")
    LiveData<WeatherForecast> getById(int id);

    @Query("SELECT * FROM weatherforecast WHERE locationId = (:locationId)")
    LiveData<List<WeatherForecast>> getByLocationId(int locationId);

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, JULIANDAY('now')-JULIANDAY(fromForecastTime/1000, 'unixepoch') AS diff " +
            "FROM weatherforecast " +
            "WHERE locationId = (:locationId) AND diff > -1 AND diff < 1 " +
            "ORDER BY ABS(diff) ASC LIMIT 1")
    LiveData<WeatherForecast> getDaily(int locationId);

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT *, (toForecastTime-fromForecastTime) AS dur, CAST(JULIANDAY('now') AS INTEGER)-JULIANDAY(fromForecastTime/1000, 'unixepoch') AS diff " +
            "FROM weatherforecast " +
            "WHERE locationId = (:locationId) AND diff <= -1 AND diff > -7 " +
            "GROUP BY CAST(diff AS INTEGER) HAVING MAX(dur) ORDER BY diff DESC")
    LiveData<List<WeatherForecast>> getWeekly(int locationId);

    @Insert
    long[] insert(WeatherForecast... weatherForecasts);

    @Update
    void update(WeatherForecast... weatherForecasts);

    @Delete
    void delete(WeatherForecast... weatherForecasts);

    @Query("DELETE FROM weatherforecast")
    void deleteAll();
}