package edu.uw.tcss450.team_5_tcss_450.weather.models;

import android.content.res.Resources;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import edu.uw.tcss450.team_5_tcss_450.R;

/**
 * A class that simply contains the information for the weather of a certain location
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeatherLocationInfo {
    /** The name for the location that the weather comes from */
    private String mLocationName;
    /** An array of weather data for the next 7 days */
    private WeatherDay[] mWeatherData;
    /** An array of weather for the hourly weather */
    private WeatherHour[] mWeatherHours;
    /** How the temperatures should be displayed (celsius/fahrenheit) */
    private TemperatureMode mOutputType;
    /** The actual current temperature */
    private int mCurrentTemp;
    /** The actual current weather state (cloudy/clear/...) */
    private WeatherType mCurrentWeatherStatus;

    /**
     * Get the location title for where this weather is based
     * @return String - A name for the location where this weather comes from
     */
    public String getLocationName() {
        return mLocationName;
    }

    /**
     * Gets the current temperature in the units previously specified
     * @return - An integer for the current temperature
     */
    public int getCurrentTemperature() {
        return kelvinUnitToChosenUnit(mCurrentTemp);
    }

    /**
     * Gets the current current weather state (cloudy/clear/...)
     * @return - A WeatherType Enum that represents the current Weather
     */
    public WeatherType getCurrentWeatherType() {
        return mCurrentWeatherStatus;
    }

    public void setWeatherFromOneCallApi(JSONObject result, Resources res) throws JSONException {
        // Set some info wide variables
        mLocationName = result.getString("name");
        mCurrentTemp = result.getJSONObject(
                res.getString(R.string.keys_json_weather_currentWeather)
        ).getInt(res.getString(R.string.keys_json_weather_temperature));

        // Get the weather Icon for the current weather
        mCurrentWeatherStatus = weatherIconToTypeEnum(
                ((JSONObject)(result
                        .getJSONObject(res.getString(R.string.keys_json_weather_currentWeather))
                            .getJSONArray(res.getString(R.string.keys_json_weather))
                                .get(0)))
                                    .getString(res.getString(R.string.keys_json_weather_icon)));

        mOutputType = TemperatureMode.FAHRENHEIT;


        JSONObject root = result;
        //
        if (!root.has(res.getString(R.string.keys_json_weather_errorCode))) {
            JSONArray hourlyData = root
                    .getJSONArray(res.getString(R.string.keys_json_weather_hourlyWeather));

            // Get the weather for each hour
            mWeatherHours = new WeatherHour[24];
            for (int i = 0; i < hourlyData.length() && i < 24; i++) {
                int temperature = ((JSONObject) hourlyData
                        .get(i))
                            .getInt(res.getString(R.string.keys_json_weather_temperature));
                long time = ((JSONObject) hourlyData
                        .get(i))
                            .getInt(res.getString(R.string.keys_json_weather_deltaTime));

                Date thisHour = new Date(time * 1000);
                WeatherType hourWeather = weatherIconToTypeEnum(
                        ((JSONObject)(((JSONObject) hourlyData
                                .get(i))
                                    .getJSONArray(res.getString(R.string.keys_json_weather))
                                        .get(0)))
                                            .getString(
                                                    res.getString(R.string.keys_json_weather_icon
                                            )));
                mWeatherHours[i] = new WeatherHour(thisHour, temperature, hourWeather);
            }

            JSONArray dailyData = root
                    .getJSONArray(res.getString(R.string.keys_json_weather_dailyWeather));

            // get the weather for each day
            mWeatherData = new WeatherDay[5];
            for (int i = 0; i < dailyData.length() && i < 5; i++) {
                int temperatureHigh = ((JSONObject) dailyData
                        .get(i))
                            .getJSONObject(res.getString(R.string.keys_json_weather_temperature))
                                .getInt(res.getString(R.string.keys_json_weather_highTemperature));
                int temperatureLow = ((JSONObject) dailyData
                        .get(i))
                            .getJSONObject(res.getString(R.string.keys_json_weather_temperature))
                                .getInt(res.getString(R.string.keys_json_weather_lowTemperature));
                long time = ((JSONObject) dailyData
                        .get(i))
                            .getLong(res.getString(R.string.keys_json_weather_deltaTime));
                WeatherType weatherKind = weatherIconToTypeEnum(
                        ((JSONObject)(((JSONObject) dailyData
                                .get(i))
                                    .getJSONArray(res.getString(R.string.keys_json_weather))
                                        .get(0)))
                                            .getString(res.getString(
                                                    R.string.keys_json_weather_icon
                                            )));
                Date thisDay = new Date(time * 1000);
                mWeatherData[i] = new WeatherDay(temperatureHigh, temperatureLow, weatherKind, thisDay);
            }
        } else {
            Log.e("WEATHER SERVER ERROR!", root.getString("message"));
        }
    }

    /**
     * Get the suffix for the weather unit being used
     * @return - Returns the temperature unit suffix
     */
    public String temperatureUnitString() {
        switch (mOutputType) {
            case FAHRENHEIT:
                return "°F";
            case CELSIUS:
                return "°C";
            case KELVIN:
            default:
                return "°K";
        }
    }

    /**
     * Get the single weather information object for today
     * @return
     */
    public WeatherDay getToday() {
        return mWeatherData[0];
    }

    /**
     * Creates an array that contains the weather for the next n days
     * @param n - The number of days to get the forecast for.
     *            Must be less than or equal to the number of days available
     * @return - An array of WeatherDay objects for the next n days
     */
    public WeatherDay[] getNDayForecast(int n) {
        WeatherDay[] weatherDataToReturn = new WeatherDay[n];

        for (int i = 0; i < n; i++) {
            weatherDataToReturn[i] = mWeatherData[i];
        }

        return weatherDataToReturn;
    }

    public WeatherHour[] getNHourForecase(int n) {
        WeatherHour[] hourlyWeatherDataToReturn = new WeatherHour[n];

        for (int i = 0; i < n; i++) {
            hourlyWeatherDataToReturn[i] = mWeatherHours[i];
        }

        return hourlyWeatherDataToReturn;
    }

    /**
     * Gets the units of measurement for the temperature (celsious -
     * @return
     */
    public TemperatureMode getTemperatureUnits(){
        return mOutputType;
    }

    public class WeatherHour {
        /** The time that the weather hour represents */
        private Date mTime;
        /** The temperature at this hour */
        private int mTemperature;
        /** The status of the weather at this point */
        private WeatherType mWeatherStatus;

        WeatherHour(Date time, int temperature, WeatherType weatherStatus){
            mTime = time;
            mTemperature = temperature;
            mWeatherStatus = weatherStatus;
        }

        /**
         * Get the time that this hour of weather represents
         * @return String - A time represented by a String
         */
        public String getTime() {
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(mTime);
            return String.valueOf(calendarDate.get(Calendar.HOUR_OF_DAY)) + ":00";
        }

        /**
         * Get the temperature for this hour of weather
         * @return - int the weather in the selected units
         */
        public int getTemperature() {
            return kelvinUnitToChosenUnit(mTemperature);
        }

        /**
         * Get the type of weather for this hour of weather
         * @return WeatherType - Cloudy/rainy/...
         */
        public WeatherType getWeatherStatus() {
            return mWeatherStatus;
        }

        public String temperatureUnitStringHour() {
            return temperatureUnitString();
        }
    }
    /**
     * A class that stores and provides weather information for a single day of weather.
     */
    public class WeatherDay {
        /** The lowest expected temp in celsius */
        private int mLowTemp;

        /** The highest expected temp in celsius */
        private int mHighTemp;

        /** The expected kind of day (clear, cloudy...) */
        private WeatherType mWeatherStatus;

        /** The date for this day of weather */
        private Date mDay;


        public WeatherDay(int highTemp, int lowTemp, WeatherType weatherStatus, Date calendarDate) {
            mLowTemp = lowTemp;
            mHighTemp = highTemp;
            mWeatherStatus = weatherStatus;
            mDay = calendarDate;
        }

        /**
         * Gets the high temperature for this day
         * @return int - the temperature in whatever units have been chosen
         */
        public int getHighTemp() {
            return kelvinUnitToChosenUnit(mHighTemp);
        }

        /**
         * Gets the low temperature for this day
         * @return int - the temperature in whatever units have been chosen
         */
        public int getLowTemp() {
            return kelvinUnitToChosenUnit(mLowTemp);
        }

        /**
         * Get the type of weather for this day
         * @return WeatherType - Cloudy/rainy/...
         */
        public WeatherType getStatus() {
            return mWeatherStatus;
        }

        /**
         * Returns the weather resource's image icon id
         * @return - The resource id
         */
        public int getIconResourceId() {
            return getWeatherIconResourceId(mWeatherStatus);
        }

        /**
         * Takes the date for this weather and returns in as an index number where 0 is sunday and
         * 6 is saturday
         *
         * @return int - The index for the weekday
         */
        public int getWeekday() {
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(mDay);
            return calendarDate.get(Calendar.DAY_OF_WEEK);
        }

        /**
         * Returns the short string for the weekday
         */
        public String getWeekdayString() {
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(mDay);
            return calendarDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
        }

        public String temperatureUnitStringDay() {
            return temperatureUnitString();
        }
    }

    /**
     * Takes into account the chosen unit for temperature display and converts to that temperature
     *
     * @param temperature - The temperature to convert in celsius
     * @return int - The new temperature value
     */
    private int kelvinUnitToChosenUnit(int temperature) {
        switch (mOutputType) {
            case FAHRENHEIT:
                return kelvinToFahrenheit(temperature);
            case CELSIUS:
                return kelvinToCelsius(temperature);
            default:
                return temperature;
        }
    }

    /**
     * Takes a type of weather and returns the resource id for the type of weathers image resource
     * @param typeToGetIdFor - The WeatherType object, rainy, cloudy...
     * @return - The resource id
     */
    public static int getWeatherIconResourceId(WeatherType typeToGetIdFor) {
        switch (typeToGetIdFor) {
            case CLEAR:
                return R.drawable.ic_clear_sky_night_128dp;
            case RAIN:
                return R.drawable.ic_rain_night_128dp;
            case SCATTERED_CLOUDS:
                return R.drawable.ic_scattered_clouds_128dp;
            case BROKEN_CLOUDS:
                return R.drawable.ic_broken_clouds_128dp;
            case FEW_CLOUDS:
            case CLOUDY:
                return R.drawable.ic_few_clouds_night_128dp;
            case THUNDER:
                return R.drawable.ic_thunderstorm_128dp;
            case SNOW:
                return R.drawable.ic_snow_128dp;
            default:
                return R.drawable.ic_rain_night_128dp;
        }
    }

    /**
     * Converts fahrenheit to celsius.
     *
     * @param temperature int - The input in celsius
     * @return int - The output in fahrenheit
     */
    private static int kelvinToFahrenheit(int temperature) {
        return (int) ((temperature - 273) * (9f/5f) + 32);
    }

    /**
     * Converts kelvin to celsius
     * @param temperature - The temp in kelvin
     * @return int - the temp in celsius
     */
    private static int kelvinToCelsius(int temperature) {
        return temperature - 273;
    }

    /**
     * Turns the icon string into a weathertype for later usage
     * @param icon - The string for the weather icon image
     * @return - The WeatherType associated with that weather icon
     */
    public static WeatherType weatherIconToTypeEnum(String icon) {
        switch(icon.substring(0, 2)) {
            case "02":
                return WeatherType.FEW_CLOUDS;
            case "03":
                return WeatherType.SCATTERED_CLOUDS;
            case "04":
                return WeatherType.BROKEN_CLOUDS;
            case "09":
                return WeatherType.SHOWER_RAIN;
            case "10":
                return WeatherType.RAIN;
            case "11":
                return WeatherType.THUNDER;
            case "13":
                return WeatherType.SNOW;
            case "50":
                return WeatherType.MIST;
            default:
            case "01":
                return WeatherType.CLEAR;
        }
    }

    /**
     * Weather types such as cloudy, rainy, etc...
     */
    public enum WeatherType {
        CLEAR, FEW_CLOUDS, SCATTERED_CLOUDS, BROKEN_CLOUDS, CLOUDY,
        SHOWER_RAIN,  RAIN, SNOW, THUNDER, MIST
    }

    /**
     * Temperature display units
     */
    public enum TemperatureMode {
        FAHRENHEIT, CELSIUS, KELVIN
    }
}
