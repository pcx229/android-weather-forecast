package com.weather.data;

import android.app.Application;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class XMLWeatherForecastParser {

    public static final String TAG = "XMLWEATHERFORECASTPARSER";

    public static final String url = "https://ims.gov.il/sites/default/files/ims_data/xml_files/isr_cities_1week_6hr_forecast.xml";

    public static final String XML_FORECAST_IMPORT_FILE_PREFIX = "XML_Weather_Forecast_",
                                XML_FORECAST_IMPORT_FILE_SUFFIX =  ".xml";

    private static final String ns = null;

    public static class Entry {
        public Location location;
        public List<WeatherForecast> forecast;

        private Entry() {
            location = null;
            forecast = new ArrayList<>();
        }
    }

    private File cashFolder;

    public XMLWeatherForecastParser(Application application) {
        cashFolder = application.getExternalCacheDir();
        // clear previews forecasts saved
        for (File f : cashFolder.listFiles()) {
            if(f.isFile() && f.getName().endsWith(XML_FORECAST_IMPORT_FILE_SUFFIX) && f.getName().startsWith(XML_FORECAST_IMPORT_FILE_PREFIX)) {
                f.delete();
            }
        }
    }

    public List<Entry> getWeatherInformationFromTheWeb() {
        try {
            File xmlWeatherForecastOutput = File.createTempFile(XML_FORECAST_IMPORT_FILE_PREFIX, XML_FORECAST_IMPORT_FILE_SUFFIX, cashFolder);
            Log.d(TAG, "downloading weather forecast data from the web...");
            downloadFile(url, xmlWeatherForecastOutput);
            Log.d(TAG, "parsing weather forecast data");
            InputStream in = new FileInputStream(xmlWeatherForecastOutput);
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, "utf-8");
            parser.nextTag();
            List<XMLWeatherForecastParser.Entry> data = readLocationForecasts(parser);
            Log.d(TAG, "number of location found: " + data.size());
            in.close();
            xmlWeatherForecastOutput.delete();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Entry> readLocationForecasts(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "LocationForecasts");
        List<Entry> entries = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Location")) {
                entries.add(readLocation(parser));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private Entry readLocation(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        Entry entry = new Entry();
        parser.require(XmlPullParser.START_TAG, ns, "Location");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("LocationMetaData")) {
                entry.location = readLocationMetaData(parser);
            } else if (name.equals("LocationData")) {
                entry.forecast = readLocationData(parser);
            } else {
                skip(parser);
            }
        }
        return entry;
    }

    private Location readLocationMetaData(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "LocationMetaData");
        Location location = new Location();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("LocationId")) {
                location.id = readIntTag(parser, "LocationId");
            } else if (name.equals("LocationNameEng")) {
                location.name = readTextTag(parser, "LocationNameEng");
            } else if (name.equals("DisplayLat")) {
                location.lat = readFloatTag(parser, "DisplayLat");
            } else if (name.equals("DisplayLon")) {
                location.lon = readFloatTag(parser, "DisplayLon");
            } else if (name.equals("DisplayHeight")) {
                location.height = readFloatTag(parser, "DisplayHeight");
            } else {
                skip(parser);
            }
        }
        return location;
    }

    private List<WeatherForecast> readLocationData(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "LocationData");
        List<WeatherForecast> forecasts = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Forecast")) {
                forecasts.add(readForecast(parser));
            } else {
                skip(parser);
            }
        }
        return forecasts;
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private static Date addHoursToJavaUtilDate(Date date, int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }

    private WeatherForecast readForecast(XmlPullParser parser) throws XmlPullParserException, IOException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "Forecast");
        WeatherForecast forecast = new WeatherForecast();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("ForecastTime")) {
                forecast.fromForecastTime = sdf.parse(readTextTag(parser, "ForecastTime"));
                forecast.toForecastTime = addHoursToJavaUtilDate(forecast.fromForecastTime, 6);
            } else if (name.equals("Temperature")) {
                forecast.temperature = readFloatTag(parser, "Temperature");
            } else if (name.equals("RelativeHumidity")) {
                forecast.humidity = readFloatTag(parser, "RelativeHumidity");
            } else if (name.equals("WindSpeed")) {
                forecast.windSpeed = readFloatTag(parser, "WindSpeed");
            } else if (name.equals("WindDirection")) {
                forecast.windDirection = readFloatTag(parser, "WindDirection");
            } else if (name.equals("Rain")) {
                forecast.rainFall = readFloatTag(parser, "Rain");
            } else if (name.equals("WeatherCode")) {
                forecast.weatherCode = readIntTag(parser, "WeatherCode");
            } else if (name.equals("MinTemp")) {
                forecast.minTemperature = readFloatTag(parser, "MinTemp");
            } else if (name.equals("MaxTemp")) {
                forecast.maxTemperature = readFloatTag(parser, "MaxTemp");
            } else {
                skip(parser);
            }
        }
        return forecast;
    }

    private float readFloatTag(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        float n = readFloat(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return n;
    }

    private float readFloat(XmlPullParser parser) throws IOException, XmlPullParserException {
        float result = 0;
        if (parser.next() == XmlPullParser.TEXT) {
            result = Float.parseFloat(parser.getText());
            parser.nextTag();
        }
        return result;
    }

    private int readIntTag(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        int n = readInt(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return n;
    }

    private int readInt(XmlPullParser parser) throws IOException, XmlPullParserException {
        int result = 0;
        if (parser.next() == XmlPullParser.TEXT) {
            result = Integer.parseInt(parser.getText());
            parser.nextTag();
        }
        return result;
    }

    private String readTextTag(XmlPullParser parser, String name) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, name);
        String text = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, name);
        return text;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private static void downloadFile(String url, File outputFile) throws Exception {
        DataInputStream wis = null;
        DataOutputStream fos = null;
        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Bad HTTP response");
            }

            wis = new DataInputStream(u.openStream());
            fos = new DataOutputStream(new FileOutputStream(outputFile));

            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = wis.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (fos != null)
                    fos.close();
                if (wis != null)
                    wis.close();
            } catch (IOException ignored) {
            }

            if (conn != null)
                conn.disconnect();
        }
    }
}
