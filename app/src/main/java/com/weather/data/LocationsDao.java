package com.weather.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LocationsDao {
    @Query("SELECT * FROM location")
    LiveData<List<Location>> getAll();

    @Query("SELECT * FROM location")
    List<Location> _getAll();

    @Query("SELECT * FROM location WHERE id IN (:ids)")
    LiveData<List<Location>> getByIds(int... ids);

    @Query("SELECT * FROM location WHERE id = (:id)")
    LiveData<Location> getById(int id);

    @Query("SELECT * FROM location WHERE id = (:id)")
    Location _getById(int id);

    @Insert
    long[] insert(Location... locations);

    @Update
    void update(Location... locations);

    @Delete
    void delete(Location... locations);

    @Query("DELETE FROM location")
    void deleteAll();
}