package edu.uw.tcss450.team_5_tcss_450.weather.views;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherLocationInfo;

/**
 * A Recycler view adapter for the hourly weather view columns
 */
public class HourlyWeatherRecyclerViewAdapter extends RecyclerView.Adapter<HourlyWeatherHolder> {
    /** The hours of weather to be drawn */
    private final List<WeatherLocationInfo.WeatherHour> mHoursOfWeather;

    public HourlyWeatherRecyclerViewAdapter(List<WeatherLocationInfo.WeatherHour> hours) {
        this.mHoursOfWeather = hours;
    }

    @NonNull
    @Override
    public HourlyWeatherHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HourlyWeatherHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.hourly_weather_row_view, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull HourlyWeatherHolder holder, int position) {
        holder.setWeather(mHoursOfWeather.get(position));
    }

    @Override
    public int getItemCount() {
        return this.mHoursOfWeather.size();
    }
}
