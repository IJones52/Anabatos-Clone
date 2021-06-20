package edu.uw.tcss450.team_5_tcss_450.weather.views;


import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherLocationInfo;
import edu.uw.tcss450.team_5_tcss_450.databinding.WeeklyWeatherColumnViewBinding;

/**
 * A view component for a single column of weather (a single day)
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeeklyWeatherColumnView extends LinearLayout {
    private WeeklyWeatherColumnViewBinding mBinding;

    public WeeklyWeatherColumnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBinding = WeeklyWeatherColumnViewBinding.inflate(LayoutInflater.from(context),this, true);
    }

    /**
     * Sets the weather that is to be displayed in this component
     * @param data - The WeatherDay object for weather information
     */
    public void setWeather(WeatherLocationInfo.WeatherDay data) {
        mBinding.textHigh.setText(String.valueOf(data.getHighTemp()) + data.temperatureUnitStringDay());
        mBinding.textLow.setText(String.valueOf(data.getLowTemp()) + data.temperatureUnitStringDay());
        Resources r = getResources();
        Log.d("Weather column", data.getStatus().toString());
        mBinding.imageWeatherstatus.setImageResource(data.getIconResourceId());
        mBinding.textWeekday.setText(data.getWeekdayString());
    }
}
