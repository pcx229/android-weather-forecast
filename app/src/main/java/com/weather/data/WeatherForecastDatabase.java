package com.weather.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Location.class, WeatherForecast.class}, version = 6, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class WeatherForecastDatabase extends RoomDatabase {

    public static final String DB_WEATHER_FORECAST_NAME = "weather_forecast";

    public static final String DB_WEATHER_FORECAST_IMPORT_NAME = "weather_forecast_import";

    private static WeatherForecastDatabase instance;
    private static Builder<WeatherForecastDatabase> importInstance;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static RoomDatabase.Callback sRoomTestDataDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            /*  Test data
            databaseWriteExecutor.execute(() -> {
                LocationsDao locationsDao = instance.locationsDao();
                locationsDao.deleteAll();

                long[] locationsIds = locationsDao.insert(new Location[] {
                        new Location("Hifa", 1, 2, 0),
                        new Location("Tel Aviv", 1, 2, 0),
                        new Location("Jerusalem", 1, 2, 0)
                });

                WeatherForecastDao weatherForecastDao = instance.weatherForecastDao();
                weatherForecastDao.deleteAll();

                weatherForecastDao.insert(new WeatherForecast[] {
                        new WeatherForecast((int) locationsIds[0], new Date(System.currentTimeMillis()+2*24*60*60*1000), new Date(System.currentTimeMillis()+100+2*24*60*60*1000), 31.5f, 32, 28, 2, 0, 0, 30, 0.3f, 0, 1020),
                        new WeatherForecast((int) locationsIds[0], new Date(System.currentTimeMillis()+3*24*60*60*1000), new Date(System.currentTimeMillis()+1000+3*24*60*60*1000), 32.5f, 32, 28, 2, 0, 0, 30, 0.3f, 0, 1160),
                        new WeatherForecast((int) locationsIds[0], new Date(System.currentTimeMillis()+1*24*60*60*1000), new Date(System.currentTimeMillis()+1000+1*24*60*60*1000), 32.5f, 32, 28, 2, 0, 0, 30, 0.3f, 0, 1250),
                        new WeatherForecast((int) locationsIds[0], new Date(System.currentTimeMillis()), new Date(System.currentTimeMillis()+10000), 33.5f, 32, 28, 2, 0, 0, 30, 0.3f, 0, 1020)
                });
            });
             */
        }
    };

    public static synchronized WeatherForecastDatabase getImportInstance(File imported) {
        return importInstance.createFromAsset(imported.getAbsolutePath()).build();
    }

    public static synchronized WeatherForecastDatabase getInstance(Context context) {
        if(instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), WeatherForecastDatabase.class, DB_WEATHER_FORECAST_NAME)
                    .fallbackToDestructiveMigration()
                    .addCallback(sRoomTestDataDatabaseCallback)
                    .build();
            importInstance = Room.databaseBuilder(context.getApplicationContext(), WeatherForecastDatabase.class, DB_WEATHER_FORECAST_IMPORT_NAME);
        }
        return instance;
    }

    public abstract LocationsDao locationsDao();
    public abstract WeatherForecastDao weatherForecastDao();
}