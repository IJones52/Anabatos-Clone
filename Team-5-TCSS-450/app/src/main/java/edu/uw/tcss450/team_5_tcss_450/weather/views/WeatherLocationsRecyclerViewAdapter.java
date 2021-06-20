package edu.uw.tcss450.team_5_tcss_450.weather.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.weather.models.LocationListInfo;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherLocationsListModel;

/**
 * A recyclerView Adapter for the rows of weather locations
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeatherLocationsRecyclerViewAdapter extends RecyclerView.Adapter<WeatherLocationHolder> {
    private final WeatherLocationsListModel mLocationListModel;
    private final View mRootView;

    public WeatherLocationsRecyclerViewAdapter(WeatherLocationsListModel locationModel, View rootView) {
        this.mRootView = rootView;
        this.mLocationListModel = locationModel;
    }

    @NonNull
    @Override
    public WeatherLocationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WeatherLocationHolder(
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(R.layout.weather_location_row_view, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull WeatherLocationHolder holder, int position) {
        holder.setLocation(mLocationListModel.getData().get(position), mRootView);
        holder.mBinding.buttonLocationDelete.setOnClickListener(
            (button) -> {
                mLocationListModel.deleteLocation(mLocationListModel.getData().get(position).getName());
                //notifyItemRemoved(position);
                notifyItemRangeChanged(position, mLocationListModel.getData().size());
            }
        );
    }

    @Override
    public int getItemCount() {
        return this.mLocationListModel.getData().size();
    }
}
