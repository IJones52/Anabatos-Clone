package edu.uw.tcss450.team_5_tcss_450;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import edu.uw.tcss450.team_5_tcss_450.model.PushyTokenViewModel;
import me.pushy.sdk.Pushy;

import android.os.Bundle;
import android.util.Log;

/**
 * Auth Activity class
 */
public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Pushy.listen(this);
        initiatePushyTokenRequest();

        SharedPreferences prefs =
                getSharedPreferences(
                        "Theme",
                        Context.MODE_PRIVATE);
        if(prefs.getBoolean("Night Mode", false)){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.Theme_NightMode);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setTheme(R.style.Theme_DayMode);
        }
    }

    private void initiatePushyTokenRequest() {
        new ViewModelProvider(this).get(PushyTokenViewModel.class).retrieveToken();
    }
}