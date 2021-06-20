package edu.uw.tcss450.team_5_tcss_450.weather.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.GridLayoutManager;

import java.util.Arrays;

import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherLocationInfo;
import edu.uw.tcss450.team_5_tcss_450.databinding.WeatherViewBinding;

/**
 * The class for the WeatherView component
 * This component allows the weather to be displayed from anywhere it needs to be
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeatherView extends RelativeLayout {
    private WeatherViewBinding mBinding;
    /** The weekly column components */
    private WeeklyWeatherColumnView[] weeks;

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //inflate(context, R.layout.weather_view, this);
        mBinding = WeatherViewBinding.inflate(LayoutInflater.from(context),this, true);
        // Store the week components in an array for easy access
        weeks = new WeeklyWeatherColumnView[5];
        weeks[0] = mBinding.weeklyDay1;
        weeks[1] = mBinding.weeklyDay2;
        weeks[2] = mBinding.weeklyDay3;
        weeks[3] = mBinding.weeklyDay4;
        weeks[4] = mBinding.weeklyDay5;

        // Set up the recycler for the hourly view
        mBinding.recyclerHourly.setLayoutManager(new GridLayoutManager(getContext(), 1));


    }

    /**
     * Sets the weather that is to be displayed in this component
     * @param data - The WeatherLocationInfo object that contains the info information
     */
    public void setWeather(WeatherLocationInfo data) {
        if (data != null) {
            mBinding.textLocation.setText(data.getLocationName());
            mBinding.imageWeatherStatus.setImageResource(data.getToday().getIconResourceId());
            mBinding.recyclerHourly.setAdapter(new HourlyWeatherRecyclerViewAdapter(Arrays.asList(data.getNHourForecase(24))));
            mBinding.textLocation.setText(data.getLocationName());
            mBinding.textCurrtemp.setText(String.valueOf(data.getCurrentTemperature()) + data.temperatureUnitString());
            mBinding.textHightemp.setText(String.valueOf(data.getNDayForecast(1)[0].getHighTemp()) + data.temperatureUnitString());
            mBinding.textLowtemp.setText(String.valueOf(data.getNDayForecast(1)[0].getLowTemp()) + data.temperatureUnitString());

            WeatherLocationInfo.WeatherDay[] weeklyWeather = data.getNDayForecast(5);
            //weeks[0].setWeather(weeklyWeather[0]);
            for (int i = 0; i < weeks.length; i++) {
                weeks[i].setWeather(weeklyWeather[i]);
            }
        } else {
            Log.d("Weather Setting Error", "Weather was null :( probably a bad connection");
        }
    }
}
