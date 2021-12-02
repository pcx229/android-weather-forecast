package com.weather;

import android.widget.EditText;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseMethod;

import com.github.pwittchen.weathericonview.WeatherIconView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Common {
    public static String getWeatherDescriptionFromCode(int code) {
        switch(code) {
            case 1010:
                return "Sandstorm";
            case 1020:
                return "Thunderstorms";
            case 1060:
                return "Snow";
            case 1070:
                return "Light snow";
            case 1080:
                return "Sleet";
            case 1140:
                return "Rainy";
            case 1160:
                return "Fog";
            case 1220:
                return "Partly cloudy";
            case 1230:
                return "Cloudy";
            case 1250:
                return "Clear";
            case 1260:
                return "Windy";
            case 1270:
                return "Muggy";
            case 1300:
                return "Frost";
            case 1310:
                return "Hot";
            case 1320:
                return "Cold";
            case 1510:
                return "Stormy";
            case 1520:
                return "Heavy snow";
            case 1530:
                return "Partly cloudy, possible rain";
            case 1540:
                return "Cloudy, possible rain";
            case 1560:
                return "Cloudy, light rain";
            case 1570:
                return "Dust";
            case 1580:
                return "Extremely hot";
            case 1590:
                return "Extremely cold";
        }
        return "Unknown Weather Type";
    }

    public static int getWeatherIconFromCode(int code) {
        switch(code) {
            case 1010:
                return R.string.wi_sandstorm;
            case 1020:
                return R.string.wi_thunderstorm;
            case 1060:
                return R.string.wi_snow;
            case 1070:
                return R.string.wi_snow;
            case 1080:
                return R.string.wi_sleet;
            case 1140:
                return R.string.wi_rain;
            case 1160:
                return R.string.wi_fog;
            case 1220:
                return R.string.wi_wu_partlycloudy;
            case 1230:
                return R.string.wi_cloudy;
            case 1250:
                return R.string.wi_day_sunny;
            case 1260:
                return R.string.wi_windy;
            case 1270:
                return R.string.wi_day_sunny;
            case 1300:
                return R.string.wi_snowflake_cold;
            case 1310:
                return R.string.wi_hot;
            case 1320:
                return R.string.wi_snowflake_cold;
            case 1510:
                return R.string.wi_storm_showers;
            case 1520:
                return R.string.wi_snow;
            case 1530:
                return R.string.wi_day_showers;
            case 1540:
                return R.string.wi_showers;
            case 1560:
                return R.string.wi_showers;
            case 1570:
                return R.string.wi_dust;
            case 1580:
                return R.string.wi_hot;
            case 1590:
                return R.string.wi_snowflake_cold;
        }
        return R.string.wi_na;
    }

    @BindingAdapter("weatherIconResource")
    public static void setWeatherIconResource(WeatherIconView weatherIconView, int resource) {
        weatherIconView.setIconResource(weatherIconView.getContext().getString(resource));
    }

    public static class FloatConverter {

        @InverseMethod("stringToFloat")
        public static String floatToString(float value) {
            return value + "";
        }

        public static float stringToFloat(String value) {
            float num = 0;
            try{
                num = Float.parseFloat(value);
            } catch (Exception e) {}
            return num;
        }
    }

    public static class IntConverter {

        @InverseMethod("stringToInt")
        public static String intToString(int value) {
            return value + "";
        }

        public static int stringToInt(String value) {
            int num = 0;
            try{
                num = Integer.parseInt(value);
            } catch (Exception e) {}
            return num;
        }
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);

    public static class DateConverter {

        @InverseMethod("stringToDate")
        public static String dateToString(Date value) {
            if(value == null) {
                return null;
            }
            return sdf.format(value);
        }

        public static Date stringToDate(String value) {
            Date date = null;
            try {
                date = sdf.parse(value);
            } catch (Exception e) {}
            return date;
        }
    }

    public static String getDateAsString(Date value) {
        if(value == null) {
            return "none";
        }
        return sdf.format(value);
    }

    private static final SimpleDateFormat dow = new SimpleDateFormat("EEEE", Locale.ENGLISH);

    public static String getDayOfWeekText(Date date) {
        return dow.format(date);
    }

    public static String getTodayDayOfWeekText() {
        return dow.format(Calendar.getInstance().getTime().getTime());
    }
}
