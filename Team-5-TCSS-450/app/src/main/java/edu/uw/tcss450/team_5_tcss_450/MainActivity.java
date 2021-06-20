package edu.uw.tcss450.team_5_tcss_450;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import edu.uw.tcss450.team_5_tcss_450.databinding.ActivityMainBinding;

import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentConnectionsBinding;

import edu.uw.tcss450.team_5_tcss_450.home.HomeFragment;

import edu.uw.tcss450.team_5_tcss_450.home.HomeFragmentDirections;


import edu.uw.tcss450.team_5_tcss_450.model.NewConnectionCountViewModel;

import edu.uw.tcss450.team_5_tcss_450.model.NewMessageCountViewModel;
import edu.uw.tcss450.team_5_tcss_450.model.PushyTokenViewModel;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.notifications.Notification;
import edu.uw.tcss450.team_5_tcss_450.notifications.NotificationListViewModel;
import edu.uw.tcss450.team_5_tcss_450.services.PushReceiver;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatIdViewModel;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatListViewModel;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatRoomFragment;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatRoomFragmentArgs;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.Connection;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatMessage;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatViewModel;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.ConnectionsViewModel;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherInfoModel;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherLocationsListModel;


import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * MainActivity creates a window for the activity_main.xml layout. Will also create the bottom
 * navigation bar that will navigate to home, weather, chat, and contacts.
 *
 * @author Danieyll Wilson
 * @version June 1, 2021
 */
public class MainActivity extends AppCompatActivity {
    /**
     * The desired interval for location updates.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * A constant for the locations permissions code
     */
    private static final int MY_PERMISSIONS_LOCATIONS = 8414;

    /**
     * Binding for MainActivity
     */
    private ActivityMainBinding mBinding;
    private MainActivityArgs mArgs;

    /**
     * The broadcast receiver that listens for messages
     */
    private MainPushMessageReceiver mPushMessageReceiver;

    /**
     * The broadcast receiver for connections
     */
    private ConnectionsPushMessageReceiver mConnectionsReceiver;

    /**
     * View model that counts number of new msgs when user not on Chat Fragment
     */
    private NewMessageCountViewModel mNewMessageModel;

    /**
     * View model that counts the number of new connections requests/connections
     */
    private NewConnectionCountViewModel mNewConnectionModel;

    /**
     * App bar Configuration variable
     */
    private AppBarConfiguration mAppBarConfiguration;

    /**
     * The fused client for getting the current location
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * The weather info model for getting the weather
     */
    private WeatherInfoModel mWeatherInfoModel;

    /**
     * The Location call back class to be called when there is new location data
     */
    private LocationCallback mLocationCallback;

    /**
     * The description for location requests
     */
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs =
                getSharedPreferences(
                        "Theme",
                        Context.MODE_PRIVATE);
        if (prefs.getBoolean("Night Mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.Theme_NightMode);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.Theme_DayMode);
        }
        // Set binding
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        // Creates bottom menu bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Instantiate NewMessageModel + add an observer to display notification badges
        mNewMessageModel = new ViewModelProvider(this).get(NewMessageCountViewModel.class);
        mNewMessageModel.addTotalMsgCountObserver(this, count -> {
            BadgeDrawable badge = mBinding.navView.getOrCreateBadge(R.id.navigation_chat);
            badge.setMaxCharacterCount(3);
            if (count > 0) {
                badge.setNumber(count);
                badge.setVisible(true);
            } else {
                badge.clearNumber();
                badge.setVisible(false);
            }
        });


        //Set up connections count model
        mNewConnectionModel = new ViewModelProvider(this)
                .get(NewConnectionCountViewModel.class);
        //Set up connections navbar badges
        mNewConnectionModel.addIncomingCountObserver(this, count -> {
            BadgeDrawable badge = mBinding.navView.getOrCreateBadge(R.id.navigation_contacts);
            badge.setMaxCharacterCount(2);
            if (count > 0) {
                badge.setNumber(count);
                badge.setVisible(true);
            } else {
                badge.clearNumber();
                badge.setVisible(false);
            }
        });


        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.navigation_home,
                R.id.navigation_contacts,
                R.id.navigation_weather,
                R.id.navigation_chat)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        WeatherLocationsListModel model = new ViewModelProvider(this)
                .get(WeatherLocationsListModel.class);
        model.fetchLocations();

        WeatherInfoModel modelWeather = new ViewModelProvider(this).get(WeatherInfoModel.class);


        //Set up the UserInfoViewModel
        mArgs = MainActivityArgs.fromBundle(getIntent().getExtras());
        new ViewModelProvider(
                this,
                new UserInfoViewModel.UserInfoViewModelFactory(mArgs.getEmail(), mArgs.getJwt(),
                        mArgs.getNickname(),
                        mArgs.getUsername(), mArgs.getMemberid()))
                .get(UserInfoViewModel.class);

        setUpLocation();

        // Example for using the weatherInfo component
        /*WeatherLocationInfo gen = new WeatherLocationInfo();
        gen.setWeather();
        mBinding.weatherInfo.setWeather(gen);*/


        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (destination.getId() == R.id.navigation_contacts || destination.getId() == R.id.connectionRequestFragment) {
                mNewConnectionModel.resetIncoming();
                if(destination.getId() == R.id.connectionRequestFragment){
                    //Remove the connections notifications
                    NotificationListViewModel mNotiModel = new ViewModelProvider(MainActivity.this)
                            .get(NotificationListViewModel.class);
                    mNotiModel.removeConnectionsNotifications();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drop_down, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_log_out) {
            logOut();
            return true;

        } else if (id == R.id.action_setting) {
            settings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_LOCATIONS);
        } else {
            mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Got last known location. In some rare situations this can be null.
                            Log.d("LOCATION UPDATE!", location.toString());
                            if (mWeatherInfoModel == null) {
                                mWeatherInfoModel = new ViewModelProvider(MainActivity.this)
                                        .get(WeatherInfoModel.class);
                            }
                            mWeatherInfoModel.setCurrentLocation(location);
                        }
                    }
                });
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // model with location data
                    Log.d("LOCATION UPDATE!", location.toString());
                    if (mWeatherInfoModel == null) {
                        mWeatherInfoModel = new ViewModelProvider(MainActivity.this)
                                .get(WeatherInfoModel.class);
                    }
                    mWeatherInfoModel.setCurrentLocation(location);
                }};
        };

        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void settings(){
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(
                HomeFragmentDirections.actionNavigationHomeToNavigationSetting());
    }

    private void logOut() {
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        prefs.edit().remove(getString(R.string.keys_prefs_jwt)).apply();

        PushyTokenViewModel mModel = new ViewModelProvider(this)
                .get(PushyTokenViewModel.class);
        //when we hear back from the web service quit
        mModel.addResponseObserver(this, result -> finishAndRemoveTask());
        mModel.deleteTokenFromWebservice(
                new ViewModelProvider(this)
                        .get(UserInfoViewModel.class)
                        .getmJwt());
    }

    protected void onResume() {
        // instantiate + register the receiver
        super.onResume();
        if (mPushMessageReceiver == null) {
            mPushMessageReceiver = new MainPushMessageReceiver();
        }
        if (mConnectionsReceiver == null) {
            mConnectionsReceiver = new ConnectionsPushMessageReceiver();
        }

        IntentFilter intentFilter = new IntentFilter(PushReceiver.RECEIVED_NEW_MESSAGE);
        registerReceiver(mPushMessageReceiver, intentFilter);

        IntentFilter connFilter = new IntentFilter(PushReceiver.RECEIVED_CONNECTION_UPDATE);
        registerReceiver(mConnectionsReceiver, connFilter);

        // Handle location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    @Override
    protected void onPause() {
        // unregister the receiver
        super.onPause();
        if (mPushMessageReceiver != null) {
            unregisterReceiver(mPushMessageReceiver);
        }
        if (mConnectionsReceiver != null) {
            unregisterReceiver(mConnectionsReceiver);
        }

        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * A BroadcastReceiver that listens for messages sent from PushReceiver.
     */
    private class MainPushMessageReceiver extends BroadcastReceiver {
        /**
         * The chat id view model.
         */
        private ChatIdViewModel mChatIdViewModel = new ViewModelProvider(MainActivity.this)
                .get(ChatIdViewModel.class);
        /**
         * The chat message view model.
         */
        private ChatViewModel mChatModel = new ViewModelProvider(MainActivity.this)
                .get(ChatViewModel.class);
        /**
         * The user info view model.
         */
        private UserInfoViewModel mUserModel = new ViewModelProvider(MainActivity.this)
                .get(UserInfoViewModel.class);
        /**
         * The notification view model.
         */
        private NotificationListViewModel mNotiModel = new ViewModelProvider(MainActivity.this)
                .get(NotificationListViewModel.class);
        /**
         * The chat list view model.
         */
        private ChatListViewModel mChatListModel = new ViewModelProvider(MainActivity.this)
                .get(ChatListViewModel.class);
        private int lastId = -1;

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("Intent Extras", intent.getExtras().toString());

            // get the user's current destination (i.e. which fragment they're on)
            NavController nc = Navigation
                    .findNavController(MainActivity.this, R.id.nav_host_fragment);
            NavDestination nd = nc.getCurrentDestination();

            // checks if the passed in intent has a new message
            if (intent.hasExtra("chatMessage")) {
                Log.d("New Message", intent.getExtras().toString());
                ChatMessage cm = (ChatMessage) intent.getSerializableExtra("chatMessage");

                if (!(mUserModel.getEmail().contains(cm.getEmail()))) {
                    //Creates a new notification for a new message
                    Notification messageNoti = new Notification.Builder(
                            cm.getTimeStamp())
                            .addMessage("Message: " + cm.getMessage())
                            .addType("New Message from " + cm.getSender())
                            .build();

                    mNotiModel.handleNotification(messageNoti);
                }

                // inform ChatViewModel of new msg
                int id = intent.getIntExtra("chatId", -1);

                // if user is not on the chat fragment, then increment the count of new msgs
                if (nd.getId() != R.id.chatRoomFragment) {
                    mNewMessageModel.increment(id);
                } else if (nd.getId() == R.id.chatRoomFragment) {
                    mChatIdViewModel.addChatIdObserver(MainActivity.this, fragmentChatId -> {
                        if (fragmentChatId != id && fragmentChatId != lastId) {
                            mNewMessageModel.increment(id);
                            lastId = fragmentChatId;
                        }
                    });
                }

                mChatModel.addMessage(id, cm);
                mChatListModel.updateRecentMessage(id, cm);
            }
        }
    }

    private class ConnectionsPushMessageReceiver extends BroadcastReceiver {
        private ConnectionsViewModel mConnectionsModel = new ViewModelProvider(MainActivity.this)
                .get(ConnectionsViewModel.class);
        private UserInfoViewModel mUserModel = new ViewModelProvider(MainActivity.this)
                .get(UserInfoViewModel.class);
        private NotificationListViewModel mNotiModel = new ViewModelProvider(MainActivity.this)
                .get(NotificationListViewModel.class);

        @Override
        public void onReceive(Context context, Intent intent) {
            NavController nc = Navigation
                    .findNavController(MainActivity.this, R.id.nav_host_fragment);
            NavDestination nd = nc.getCurrentDestination();

            //New Connection Accepted

            if (intent.hasExtra("connections")) {

                if (nd.getId() != R.id.navigation_contacts || nd
                        .getId() != R.id.addConnectionFragment) {

                    mNewConnectionModel.incrementIncoming();
                } else {
                    mConnectionsModel.getUserID(mUserModel.getEmail());
                    mConnectionsModel.addUserIDObserver((LifecycleOwner) context, id -> {
                        if (id != 0) {
                            mConnectionsModel.getIncomingRequests(id);
                            mConnectionsModel.getConnections(id);


                        }
                    });
                }
            }
            //New Incoming Request
            if (intent.hasExtra("request")) {
                Connection c = (Connection) intent.getSerializableExtra("request");

                //Date formatting
                final DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");

                //Creates a new notification for a new message
                Date date = new Date();
                Notification connectionNoti = new Notification.Builder(
                        format.format(date))
                        .addMessage("You have received a new connection request from " + c.getName())
                        .addType("New Connection Request from " + c.getName())
                        .build();

                mNotiModel.handleNotification(connectionNoti);

                //If the user is not on the connections fragments, put a notification badge. Else refresh the incoming request list
                if (nd.getId() != R.id.connectionRequestFragment) {

                    mNewConnectionModel.incrementIncoming();
                } else {
                    mConnectionsModel.getUserID(mUserModel.getEmail());
                    mConnectionsModel.addUserIDObserver((LifecycleOwner) context, id -> {
                        if (id != 0) {
                            mConnectionsModel.getIncomingRequests(id);
                        }
                    });
                }
            }
            //Refreshes the lists as needed
            if (intent.hasExtra("update")) {

                int userId = mUserModel.getmId();
                if (intent.getStringExtra("update").equals("outgoing")) {
                    //If the user is on the outgoing page when the update happens, refresh list
                    if (nd.getId() == R.id.addConnectionFragment) {
                        mConnectionsModel.getOutgoingRequests(userId);

                    }
                } else if (intent.getStringExtra("update").equals("incoming")) {
                    if (nd.getId() == R.id.connectionRequestFragment) {

                        mConnectionsModel.getIncomingRequests(userId);

                    }
                } else if (intent.getStringExtra("update").equals("connections")) {
                    if (nd.getId() == R.id.navigation_contacts) {

                        mConnectionsModel.getConnections(userId);

                    }
                }
            }
        }
    }
}