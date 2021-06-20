package edu.uw.tcss450.team_5_tcss_450.weather.views;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.weather.fragments.WeatherLocationsFragmentDirections;
import edu.uw.tcss450.team_5_tcss_450.databinding.WeatherLocationRowViewBinding;
import edu.uw.tcss450.team_5_tcss_450.weather.models.LocationListInfo;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherInfoModel;

/**
 * Objects generated which represents the location of some weather
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeatherLocationHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public WeatherLocationRowViewBinding mBinding;
    /** The name of the location */
    private String mLocation;

    public WeatherLocationHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;
        mBinding = WeatherLocationRowViewBinding.bind(itemView);
    }

    /**
     * Set the location for a single location button
     *
     * @param location String - a string representing the name of the location
     * @param view View - The view to use for navigation purposes
     */
    void setLocation(final LocationListInfo location, View view) {
        mLocation = location.getName();
        mBinding.buttonLocation.setText(mLocation);
        mBinding.buttonLocation.setOnClickListener(
            // Set the button to go to the forecast on click
            (button) -> {
                Navigation.findNavController(view).navigate(
                    WeatherLocationsFragmentDirections
                        .actionWeatherLocationsFragmentToForecastFragment(
                            (float) location.getLat(), (float) location.getLon()
                        )
                );
            }
        );
    }
}
