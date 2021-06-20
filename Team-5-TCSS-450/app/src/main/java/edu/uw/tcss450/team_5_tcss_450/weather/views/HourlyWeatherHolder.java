package edu.uw.tcss450.team_5_tcss_450.weather.views;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherLocationInfo;
import edu.uw.tcss450.team_5_tcss_450.databinding.HourlyWeatherRowViewBinding;

/**
 * Objects generated that represent the rows of hourly weather
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class HourlyWeatherHolder extends RecyclerView.ViewHolder {
    public final View mView;
    /** The row for this weather */
    public HourlyWeatherRowViewBinding mBinding;
    /** The weather data for this hour */
    private WeatherLocationInfo.WeatherHour mWeatherHour;

    public HourlyWeatherHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
        mBinding = HourlyWeatherRowViewBinding.bind(itemView);
    }

    /**
     * Set the weather for this holder to be a certain hour of weather
     * @param weatherHour - The hour of weather
     */
    void setWeather(final WeatherLocationInfo.WeatherHour weatherHour) {
        mWeatherHour = weatherHour;
        // Set both the time and the temperature
        mBinding.textTime.setText(weatherHour.getTime());
        mBinding.textTemp.setText(String.valueOf(weatherHour.getTemperature()) + weatherHour.temperatureUnitStringHour());
    }
}
