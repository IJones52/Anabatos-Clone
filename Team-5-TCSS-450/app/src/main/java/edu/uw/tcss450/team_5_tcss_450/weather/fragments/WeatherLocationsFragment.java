package edu.uw.tcss450.team_5_tcss_450.weather.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherInfoModel;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherLocationsListModel;
import edu.uw.tcss450.team_5_tcss_450.weather.views.WeatherLocationsRecyclerViewAdapter;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentWeatherLocationsBinding;

/**
 * A Fragment that shows a selection of locations to be opened and viewed.
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeatherLocationsFragment extends Fragment {
    /** The model for all the weather locations */
    private WeatherLocationsListModel mModel;
    /** The model for the weather forecast */
    private WeatherInfoModel mWeatherLocationModel;
    private FragmentWeatherLocationsBinding mBinding;

    private UserInfoViewModel mUserModel;
    /** Whether or not the add menu is expanded and showing options */
    private boolean mMenuDisplayed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = new ViewModelProvider(getActivity()).get(WeatherLocationsListModel.class);
        mWeatherLocationModel = new ViewModelProvider(getActivity()).get(WeatherInfoModel.class);
        mUserModel = new ViewModelProvider(getActivity()).get(UserInfoViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather_locations, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the locations recycler
        mBinding = FragmentWeatherLocationsBinding.bind(getView());
        mBinding.locations.setAdapter(new WeatherLocationsRecyclerViewAdapter(mModel, getView()));
        mBinding.locations.setLayoutManager(new GridLayoutManager(getContext(), 1));

        // When the plus button is tapped, expand the menu for ways to add locations
        mBinding.buttonAddWeatherLocation.setOnClickListener((button) -> {
            if (mMenuDisplayed) {
                mBinding.buttonAddZipLocation.setVisibility(View.GONE);
            } else {
                mBinding.buttonAddZipLocation.setVisibility(View.VISIBLE);
            }
            // Toggle the displayed menu
            mMenuDisplayed = !mMenuDisplayed;
        });

        // When the add zip code button is hit, pop up a menu that allows a zip cod to be entered
        // and a location added.
        mBinding.buttonAddZipLocation.setOnClickListener((button) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View zipCodePrompt = getLayoutInflater().inflate(R.layout.dialog_add_zip_location, null);

            builder.setView(zipCodePrompt);

            builder.setTitle("Add location by zip code");
            AlertDialog zipCodeDialog = builder.create();

            // When the add button is clicked then show the progress bar
            zipCodePrompt.findViewById(R.id.button_add).setOnClickListener(
                    (add_button) -> {
                        // Get the zip code
                        String zipCodeString = ((EditText) zipCodePrompt.findViewById(R.id.editText_zip))
                                .getText().toString();
                        // Make sure it is the correct length
                        if (zipCodeString.length() == 5) {
                            zipCodePrompt.findViewById(R.id.progressBar_adding)
                                    .setVisibility(View.VISIBLE);
                            mModel.addLocationZip(mUserModel.getmJwt(), zipCodeString, (result) -> {
                                if (result == 200) {
                                    // If the result is good then dismiss the dialog
                                    zipCodePrompt.findViewById(R.id.progressBar_adding)
                                            .setVisibility(View.GONE);
                                    zipCodeDialog.dismiss();
                                } else if (result == 400 || result == 404) {
                                    // If there was an issue with finding it then say so
                                    zipCodePrompt.findViewById(R.id.progressBar_adding)
                                            .setVisibility(View.GONE);
                                    zipCodePrompt.findViewById(R.id.text_error)
                                            .setVisibility(View.VISIBLE);
                                    ((TextView) zipCodePrompt.findViewById(R.id.text_error))
                                            .setText(R.string.text_weather_noZipCode);
                                } else {
                                    // If there was a server side issue let the user know
                                    zipCodePrompt.findViewById(R.id.progressBar_adding)
                                            .setVisibility(View.GONE);
                                    zipCodePrompt.findViewById(R.id.text_error)
                                            .setVisibility(View.VISIBLE);
                                    ((TextView) zipCodePrompt.findViewById(R.id.text_error))
                                            .setText(R.string.text_weather_badZipCode);

                                }
                            });
                        } else {
                            // The zip code as invalid because it wasn't long enough
                            zipCodePrompt.findViewById(R.id.text_error)
                                    .setVisibility(View.VISIBLE);
                            ((TextView) zipCodePrompt.findViewById(R.id.text_error))
                                    .setText(R.string.text_weather_invalidZipCode);
                        }
                    }
            );

            // Show the zip code dialog to let the user interact with it
            zipCodeDialog.show();
        });

        // Whenever the locations change, change the locations being displayed
        mModel.addWeatherLocationInfoObserver(getViewLifecycleOwner(), weatherLocations -> {
            Log.d("Weather Location Observer", "Updated the model " + mModel.getData().size());
            mBinding.locations.setAdapter(new WeatherLocationsRecyclerViewAdapter(mModel, getView()));
        });

        super.onViewCreated(view, savedInstanceState);
        mBinding = FragmentWeatherLocationsBinding.bind(getView());
        mBinding.weatherCurrent.setWeather(mWeatherLocationModel.getData());
        // Whenever the forecast changes, change the forecast
        mWeatherLocationModel.addWeatherInfoObserver(getViewLifecycleOwner(), weatherInfo -> {
            mBinding.weatherCurrent.setWeather(weatherInfo);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch the weather for the current location
        mWeatherLocationModel.fetchWeather(mUserModel.getmJwt());
    }
}