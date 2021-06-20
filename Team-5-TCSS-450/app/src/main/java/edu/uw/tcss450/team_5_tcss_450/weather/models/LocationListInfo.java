package edu.uw.tcss450.team_5_tcss_450.weather.models;

/**
 * The class that contains the information for a single saved location
 *
 * @author Daniel Machen
 * @version 2021/5/30
 */
public class LocationListInfo {
    /**
     * The name of this location
     */
    private String mLocationName;

    /**
     * The lat and lon for this location
     */
    private double mLat, mLon;

    public LocationListInfo(String locationName, double lat, double lon) {
        mLocationName = locationName;
        mLat = lat;
        mLon = lon;
    }

    /**
     * Get the name for this location
     * @return String - The name of this location (Usually a city name or something)
     */
    public String getName() {
        return mLocationName;
    }

    /**
     * The latitude for this location
     * @return double - the latitude
     */
    public double getLat() {
        return mLat;
    }

    /**
     * The longitude for this location
     * @return double - the longitude
     */
    public double getLon() {
        return mLon;
    }

}
