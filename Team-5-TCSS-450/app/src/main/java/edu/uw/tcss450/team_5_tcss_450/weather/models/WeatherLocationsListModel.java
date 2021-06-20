package edu.uw.tcss450.team_5_tcss_450.weather.models;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * A list model for a list of weather locations that have been saved
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeatherLocationsListModel extends AndroidViewModel {
    /** The list of locations */
    private MutableLiveData<List<LocationListInfo>> mLocationList;

    public WeatherLocationsListModel(@NonNull Application application) {
        super(application);
        mLocationList = new MutableLiveData<>();
    }

    /**
     * This method will be called to get the weather from an api end point. For now call it whenever
     * you need data and the observer to be proc'd
     */
    public void fetchLocations() {
        List<LocationListInfo> locations = new ArrayList<>();
        locations.add(new LocationListInfo("Moscow", 55.751244, 37.618423));
        locations.add(new LocationListInfo("Seattle", 47.608013, -122.335167));
        locations.add(new LocationListInfo("Los Angeles", 34.0522, -118.2437));
        locations.add(new LocationListInfo("New York", 40.7128, -74.0060));
        locations.add(new LocationListInfo("London", 51.5074, -0.1278));

        mLocationList.setValue(locations);
    }

    /**
     * Check if a location already exists in the list
     * @param name - The name of the location
     * @return boolean - True if the location already exists in the list
     */
    public boolean hasLocation(String name) {
        for (LocationListInfo location: mLocationList.getValue()) {
            if (location.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a location based on a zip code
     * @param jwt - The authorization to request weather
     * @param zip - The zip code to use
     * @param callback - The function that should be called back when the location is either added
     *                   or failed to be added.
     */
    public void addLocationZip(String jwt, String zip, AddLocationSuccess callback) {
        Log.d("Adding Location In Model", "Starting to add");
        String url = "https://team-5-tcss-450.herokuapp.com/weather?zip=" + zip;
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                (json) -> {
                    try {
                        LocationListInfo toAdd =
                                new LocationListInfo(json.getString("name"),
                                        json.getDouble("lat"),
                                        json.getDouble("lon"));

                        mLocationList.getValue().add(toAdd);
                        mLocationList.setValue(mLocationList.getValue());
                    } catch (JSONException e) {
                        Log.d("JSON Error", "Error decoding json while adding location");
                    }
                    callback.successHandler(200);
                },
                (error) -> {
                    callback.successHandler(error.networkResponse.statusCode);
                }) {
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
     * Delete a location based on the name of the location
     * @param name - The name of the location to be deleted
     */
    public void deleteLocation(String name) {
        // Find location and delete
        ListIterator<LocationListInfo> iter = mLocationList.getValue().listIterator();
        while (iter.hasNext()) {
            if (iter.next().getName().equalsIgnoreCase(name)) {
                iter.remove();
            }
        }
        mLocationList.setValue(mLocationList.getValue());
    }

    /**
     * Gets the actual weather data object
     * @return List<String> - A list of strings that denote locations
     */
    public List<LocationListInfo> getData() {
        return mLocationList.getValue();
    }

    /**
     * Watch for updates to the weather location list
     * @param owner - The view that owns the observer
     * @param observer - The object to be called on change
     */
    public void addWeatherLocationInfoObserver(@NonNull LifecycleOwner owner,
                                       @NonNull Observer<? super List<LocationListInfo>> observer) {
        mLocationList.observe(owner, observer);
    }

    /**
     * This class allows a lambda function to be passed in and called upon success or failure of
     * adding a location.
     */
    public interface AddLocationSuccess {
        public void successHandler(int status);
    }
}
