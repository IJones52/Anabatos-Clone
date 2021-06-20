package edu.uw.tcss450.team_5_tcss_450.weather.models;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;

/**
 * A view model that will collect and contain the data to be displayed by the weather fragment
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeatherInfoModel extends AndroidViewModel {
    /** Weather information that changes as weather is requested */
    private MutableLiveData<WeatherLocationInfo> mCurrentWeather;
    /** the current location as far as we have been updated */
    private Location mCurrentLocation;
    /** If we tried to get the weather for the current location before we have the current location
        then store the jwt token here and lie in wait for the location to be updated */
    private String mWaitingForLocationJwt = null;

    public WeatherInfoModel(@NonNull Application application) {
        super(application);
        mCurrentWeather = new MutableLiveData<>();
    }

    /**
     * Sets the current location to get weather from
     * @param newLocation - The location object
     */
    public void setCurrentLocation(Location newLocation) {
        mCurrentLocation = newLocation;
        if (mWaitingForLocationJwt != null) {
            fetchWeather(mWaitingForLocationJwt);
            mWaitingForLocationJwt = null;
        }
    }


    /**
     * This method will be called to get the weather from an api end point. For now call it whenever
     * you need data and the observer to be proc'd
     */
    public void fetchWeather(String jwt) {
        // If we don't have the location yet then hold off until we get the location
        if (mCurrentLocation == null) {
            mWaitingForLocationJwt = jwt;
            return;
        }
        String url = "https://team-5-tcss-450.herokuapp.com/weather?lat=" +
                mCurrentLocation.getLatitude() +
                "&lon=" + mCurrentLocation.getLongitude();

        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                this::handleData,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                // add headers <key,value>
                headers.put("Authorization", jwt);

                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    /**
     * This method will be called to get the weather from an api end point. For now call it whenever
     * you need data and the observer to be proc'd
     */
    public void fetchWeatherByLatLon(String jwt, float lat, float lon) {
        String url = "https://team-5-tcss-450.herokuapp.com/weather?lat=" + lat + "&lon=" + lon;
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                this::handleData,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();

                // add headers <key,value>
                headers.put("Authorization", jwt);

                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }

    /**
     * Handles the results of the weather data request and puts it into the weatherLocationInfo object
     * @param result
     */
    public void handleData(final JSONObject result) {
        try {
            WeatherLocationInfo weather = new WeatherLocationInfo();
            weather.setWeatherFromOneCallApi(result, getApplication().getResources());
            mCurrentWeather.setValue(weather);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("WEATHER JSON ERROR!", e.getMessage());
        }
    }

    /**
     * Handles an error coming from volley
     * @param error
     */
    private void handleError(final VolleyError error) {
        mCurrentWeather.setValue(null);
    }

    /**
     * Gets the actual weather data object
     * @return
     */
    public WeatherLocationInfo getData() {
        return mCurrentWeather.getValue();
    }

    /**
     * The observer allows the observer to wait for the weather data to be aquired from the server
     * @param owner - The view that is watching the data
     * @param observer - The observer that will watch for the weather to be received
     */
    public void addWeatherInfoObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super WeatherLocationInfo> observer) {
        mCurrentWeather.observe(owner, observer);
    }
}
