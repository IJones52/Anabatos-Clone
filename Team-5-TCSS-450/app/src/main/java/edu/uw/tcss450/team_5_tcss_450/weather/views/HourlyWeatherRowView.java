package edu.uw.tcss450.team_5_tcss_450.weather.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherLocationInfo;
import edu.uw.tcss450.team_5_tcss_450.databinding.HourlyWeatherRowViewBinding;

/**
 * A view class for a single row of hourly weather
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class HourlyWeatherRowView extends LinearLayout {
    private HourlyWeatherRowViewBinding mBinding;

    public HourlyWeatherRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBinding = HourlyWeatherRowViewBinding.inflate(LayoutInflater.from(context),this, true);
    }

    /**
     * Sets the weather that is to be displayed in this component
     * @param data - The WeatherDay object for weather information
     */
    public void setWeather(WeatherLocationInfo.WeatherHour data) {
        mBinding.textTime.setText(data.getTime());
        mBinding.textTemp.setText(String.valueOf(data.getTemperature()) + data.temperatureUnitStringHour());
    }
}
