package edu.uw.tcss450.team_5_tcss_450.weather.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import edu.uw.tcss450.team_5_tcss_450.databinding.WeatherLocationRowViewBinding;

/**
 * A super simple view class for the weather location rows
 *
 * @author Daniel Machen
 * @version 2021/5/19
 */
public class WeatherLocationRowView extends LinearLayout {
    private WeatherLocationRowViewBinding mBinding;

    public WeatherLocationRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBinding = WeatherLocationRowViewBinding.inflate(LayoutInflater.from(context),this, true);
    }
}
