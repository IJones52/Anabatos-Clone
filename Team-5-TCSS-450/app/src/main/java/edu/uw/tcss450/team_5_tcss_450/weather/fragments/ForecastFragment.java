package edu.uw.tcss450.team_5_tcss_450.weather.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherInfoModel;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentForecastBinding;

/**
 * A fragment that displays the forecast for a location {@link Fragment}
 * Takes its information from the WeatherInfoModel.
 *
 * @author Daniel Machen
 * @version 2021/6/6
 */
public class ForecastFragment extends Fragment {
    /** The weather forecast information */
    private WeatherInfoModel mModel;
    private FragmentForecastBinding mBinding;
    private UserInfoViewModel mUserModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(getActivity()).get(WeatherInfoModel.class);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forecast, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get the navigation arguments
        ForecastFragmentArgs navArguments = ForecastFragmentArgs.fromBundle(getArguments());
        float lat = navArguments.getLat();
        float lon = navArguments.getLon();
        mBinding = FragmentForecastBinding.bind(getView());
        mBinding.weatherInfo.setWeather(null);

        // Get weather for the location giving by the navigation arguments
        mModel.fetchWeatherByLatLon(mUserModel.getmJwt(), lat, lon);
        // Whenever the forecast changes, change the forecast
        mModel.addWeatherInfoObserver(getViewLifecycleOwner(), weatherInfo -> {
            mBinding.weatherInfo.setWeather(weatherInfo);
        });
    }
}