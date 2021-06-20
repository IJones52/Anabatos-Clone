package edu.uw.tcss450.team_5_tcss_450.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentHomeBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.notifications.Notification;
import edu.uw.tcss450.team_5_tcss_450.notifications.NotificationListViewModel;
import edu.uw.tcss450.team_5_tcss_450.notifications.NotificationRecyclerViewAdapter;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.ConnectionsViewModel;
import edu.uw.tcss450.team_5_tcss_450.weather.models.WeatherInfoModel;

/**
 * Creates and binds views for fragment home..
 * A simple {@link Fragment} subclass.
 *
 * @author Danieyll Wilson
 * @version June 5, 2021
 */
public class HomeFragment extends Fragment {

    /**
     * NotificationListViewModel variable.
     */
    private NotificationListViewModel mNotiModel;

    /**
     * UserInfoViewModel variable.
     */
    private UserInfoViewModel mUserModel;

    /**
     * Notification RecyclerView Adapter variable.
     */
    private NotificationRecyclerViewAdapter noteAdapter;

    /**
     * SharedPreference variable.
     */
    private SharedPreferences sp;

    /**
     * SharedPreference Editor variable.
     */
    private SharedPreferences.Editor edit;

    /**
     * Date formatting
     */
    private final DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm aa");

    /**
     * Checks if the time is nighttime or not.
     * Initialize to false since night time is
     * only 1/3 of the day while day is 2/3.
     */
    private boolean isNight = false;

    /**
     * (Light/Dark) mode as an int.
     */
    private int currentMode;

    /**
     * Thirty days converted into a Long variable.
     */
    private final Long THIRTY_DAYS = 2592000000L;

    /**
     * Required EMPTY public constructor
     */
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNotiModel = new ViewModelProvider(getActivity()).get(NotificationListViewModel.class);

        sp = getContext().getSharedPreferences("SharedNotifications", Context.MODE_PRIVATE);
        edit = sp.edit();

        currentMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Local access to the ViewBinding object. No need to create as Instance Var as it is only
        //used here.
        FragmentHomeBinding binding = FragmentHomeBinding.bind(getView());

        mUserModel = new ViewModelProvider(getActivity())
                .get(UserInfoViewModel.class);

        ConnectionsViewModel mConnModel = new ViewModelProvider(getActivity()).get(ConnectionsViewModel.class);
        mConnModel.getUserID(mUserModel.getEmail());
        //Wait for the userid to get requests
        mConnModel.addUserIDObserver(this, id -> {
            if (!(id == 0)) {
                mConnModel.getIncomingRequests(id);
            }
        });

        insertWelcome(binding, view, mUserModel);

        insertNotifications(binding, mUserModel, mConnModel);

        insertWeather(binding, view);
    }

    @Override
    public void onResume() {
        super.onResume();
        // On navigation to the home page then get the weather for the current location
        WeatherInfoModel weatherModel = new ViewModelProvider(getActivity())
                .get(WeatherInfoModel.class);

        weatherModel.fetchWeather(mUserModel.getmJwt());
    }

    /**
     * Sets up welcoming message on the Home Fragment.
     *
     * @param binding FragmentHomeBinding, used to attach welcome text and image to the home fragment.
     * @param view view. Used to get the context.
     * @param mUserModel UserInfoViewModel used for grabbing the user first and last name.
     */
    private void insertWelcome(FragmentHomeBinding binding, @NonNull View view, UserInfoViewModel mUserModel) {

        //Sets time in military time (24 hours, use % 12.00 for AM/PM)
        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        String placeholder;

        //Sets welcome message depending on time of day
        if (time >= 5.00 && time < 12.00) {
            switch (currentMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    binding.imageHomeWelcomeMessage.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_sunrise_black_387dp));
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    binding.imageHomeWelcomeMessage.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_sunrise_white_387dp));
                    break;
            }
            placeholder = getString(R.string.text_good_morning) + mUserModel.getmUser();
            binding.textHomeWelcomeMessage.setText(placeholder);
            isNight = false;
        }
        else if (time >= 12.00 && time < 17.00) {
            switch (currentMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    binding.imageHomeWelcomeMessage.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_sun_black_227dp));
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    binding.imageHomeWelcomeMessage.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_sun_white_227dp));
                    break;
            }
            placeholder = getString(R.string.text_good_afternoon) + mUserModel.getmUser();
            binding.textHomeWelcomeMessage.setText(placeholder);
            isNight = false;
        }
        else if (time >= 17.00 && time < 21.00) {
            switch (currentMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    binding.imageHomeWelcomeMessage.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_sunset_black_387dp));
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    binding.imageHomeWelcomeMessage.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_sunset_white_387dp));
                    break;
            }
            placeholder = getString(R.string.text_good_evening) + mUserModel.getmUser();
            binding.textHomeWelcomeMessage.setText(placeholder);
            isNight = false;
        }
        else {
            switch (currentMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    binding.imageHomeWelcomeMessage.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_moon_black_313dp));
                    break;
                case Configuration.UI_MODE_NIGHT_YES:
                    binding.imageHomeWelcomeMessage.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_moon_white_313dp));
                    break;
            }
            placeholder = getString(R.string.text_good_night) + mUserModel.getmUser();
            binding.textHomeWelcomeMessage.setText(placeholder);
            isNight = true;
        }
    }

    /**
     * Sets up the Notifications and functionality inside the Notification RecyclerView
     * and sets itself up on the Home Fragment.
     *
     * @param binding FragmentHomeBinding, Used to attach the recyclerView to the home fragment.
     * @param mUserModel UserViewModel, Uses the email of the user to grab all notifications from the SharedResources.
     * @param mConnModel ConnectionViewModel, Uses the ConnectionViewModel to observe and create new notifications of
     *                   connection request/friend request.
     */
    private void insertNotifications(FragmentHomeBinding binding, UserInfoViewModel mUserModel, ConnectionsViewModel mConnModel) {

        receive(mUserModel.getEmail());

        mNotiModel.addNotificationListObserver(getViewLifecycleOwner(), notificationList -> {

            setConnNoti(mConnModel);

            final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                    /*
                    Runs through all notifications and checks if a notification is 30 days old.
                    30 days = 1000 * 60 * 60 * 24 * 30 -> (milliseconds) * (seconds) * (minutes) * (hours) * (days)
                    30 days = 2592000000L

                    Note: Check only the minutes. NOT THE SECONDS. This might be due to the formatting
                    only going to minutes and not seconds.

                    For testing, Enter this block of code into the end of onCreate of HomeFragment.java:
                        // FOR TESTING PURPOSES AND SHOWCASE ONLY: showcase the functionality of self destruct notification.
                        // Created Notification will be 1 minute before being 30 days old.
                        Date date = new Date();
                        Date dateTester = new Date(date.getTime() - 2591950000L);
                        Notification testerNoti = new Notification.Builder(
                                format.format(dateTester))
                                .addMessage("This message will self destruct after 1 minute. This is used as a tester!")
                                .addType("New Message from admin")
                                .build();
                        mNotiModel.handleNotification(testerNoti);

                        Note: If notification doesn't show up on homepage, leave homepage and return back after a short wait (5-15 seconds)
                     */
                        long expTime = new Date().getTime() - THIRTY_DAYS;
                        if (notificationList != null) {
                            for (int i = 0; i < notificationList.size(); i++) {
                                /* If recent chat notification, don't delete since date corresponds
                                   to the last sent message. So it's possible to go into a recent chat
                                   and the most recent message being 30+ days old, where shouldn't be deleted
                                */
                                if (notificationList.get(i).getType().toLowerCase().contains("recent chat")) {
                                    continue;
                                }
                                try {
                                    Date notiDate = format.parse(notificationList.get(i).getDate());
                                    if (notiDate.getTime() <= expTime) {
                                        notificationList.remove(i);
                                        //rest of the notifications are not older than 30 days
                                    } else {
                                        break;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                binding.recyclerViewHomeNotifications.setAdapter(
                                        noteAdapter = new NotificationRecyclerViewAdapter(notificationList));
                            }
                        }
                        //If "clear all" button is pressed, empties notification list and updates recyclerView arr != null &&
                        binding.actionHomeNotificationClearAll.setOnClickListener(button -> {
                            if (!notificationList.isEmpty()) {
                                notificationList.clear();
                                binding.recyclerViewHomeNotifications.setAdapter(
                                        noteAdapter = new NotificationRecyclerViewAdapter(notificationList));
                            }
                        });
                        // Notify user that there are no new notifications
                        if (notificationList.isEmpty()) {
                            binding.textHomeNotifications.setVisibility(View.VISIBLE);
                        }
                        // Hides "no new notifications" text since notification list is not empty
                        // !(arr.isEmpty()) && !(notificationList.isEmpty())
                        else {
                            binding.textHomeNotifications.setVisibility(View.INVISIBLE);
                        }
                        handler.postDelayed(this, 1000);
                    }
                }, 10);
            binding.recyclerViewHomeNotifications.setAdapter(
                    noteAdapter = new NotificationRecyclerViewAdapter(notificationList));
            setList(mUserModel.getEmail(),notificationList);
        });

        //Reverses the recyclerView so latest notifications displays on top while older ones on bottom
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        binding.recyclerViewHomeNotifications.setLayoutManager(linearLayoutManager);

        onSwipe(binding);

    }

    /**
     * Sets up the Weather for the current day and current device location on the Home Fragment.
     *
     * @param binding FragmentHomeBinding, used to attach all information from the
     *                weatherInfoModel to imageViews and textViews for the weather on the home fragment.
     */
    private void insertWeather(FragmentHomeBinding binding, @NonNull View view) {
        WeatherInfoModel weatherModel = new ViewModelProvider(getActivity())
                .get(WeatherInfoModel.class);

        switch (currentMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                binding.imageHomeWeatherBackground.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_expandable_icon_black_dp));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                binding.imageHomeWeatherBackground.setImageIcon(Icon.createWithResource(view.getContext(), R.drawable.ic_expandable_icon_white_dp));
                break;
        }

        weatherModel.addWeatherInfoObserver(getViewLifecycleOwner(), CurrentWeatherList -> {

        //Updates time without the user needing to do anything
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Date date = new Date();
                String dateStr = format.format(date);
                binding.textHomeWeatherDate.setText(dateStr);
                handler.postDelayed(this, 1000);
            }
        }, 10);

        setWeatherStatusImage(weatherModel, binding);

            //Setting Celsius textview
            String celsius = fahToCel(weatherModel.getData().getToday().getLowTemp())
                    + "/" + fahToCel(weatherModel.getData().getToday().getHighTemp()) + " C";
            binding.textHomeWeatherTempCelsius.setText(celsius);

            //Setting Fahrenheit textview
            String fahrenheit = weatherModel.getData().getToday().getLowTemp()
                    + "/" + weatherModel.getData().getToday().getHighTemp() + " F";
            binding.textHomeWeatherTempFahrenheit.setText(fahrenheit);

            //Setting current location textview
            binding.textHomeWeatherLocation.setText(weatherModel.getData().getLocationName());

            //When pressing the weather widget on home page, sends user to weather page.
            binding.textHomeWeatherBackground.setOnClickListener(button ->
                    Navigation.findNavController(getView()).navigate(
                            HomeFragmentDirections
                                    .actionNavigationHomeToNavigationWeather()
                    ));
            }
        );
    }

    /**
     * Adds Swiping ability to each notification inside an recyclerView. In this case, the swiping
     * ability is added to the home fragment notification that when swiping right to left, it will
     * remove the notification from ArrayList<Notification> arr, essentially "deleting" the notification
     * from the home fragment notification recyclerView.
     *
     * @param binding FragmentHomeBinding, used to attach the ItemTouchHelper to the notification recyclerView.
     */
    private void onSwipe(FragmentHomeBinding binding) {
        //Swipe right to left to delete
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            /**
             * Checks if Notification was moved, required function
             *
             * @param recyclerView RecyclerView, Notification Recyclerview on home page
             * @param viewHolder viewHolder, Notification
             * @param target specific Notification
             * @return boolean that is always return true
             */
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Notify the RecyclerView
                noteAdapter.notifyItemRemoved(viewHolder.getLayoutPosition());

                //remove item from list
                mNotiModel.addNotificationListObserver(getViewLifecycleOwner(), notificationList -> {
                    int position = viewHolder.getLayoutPosition();
                    notificationList.remove(position);
                });
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Get RecyclerView item from the ViewHolder
                    View itemView = viewHolder.itemView;

                    Paint paint = new Paint();

                    //draw red rectangle and trash bin behind each notification
                    if (dX < 0 && dX > -1010) {
                        //draw red background for select notification
                        paint.setColor(Color.RED);
                        c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getRight(), itemView.getBottom(), paint);
                        //draw trash bin above red background
                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_delete_forever_white_24dp, null);
                        assert drawable != null;
                        drawable.setBounds(itemView.getRight() - 144, itemView.getTop() + 30, itemView.getRight() - 20, itemView.getBottom() - 30);
                        drawable.draw(c);
                        //Catches a bug where if swiping too fast, leaves the red delete bar on the RecyclerView.
                    } else if (dX < -1010){
                        switch (currentMode) {
                            case Configuration.UI_MODE_NIGHT_NO:
                                //Paints over where the original itemView was with the default background color.
                                paint.setColor(Color.rgb(245,245,245));
                                c.drawRect(itemView.getLeft(), itemView.getTop() , itemView.getRight(), itemView.getBottom(), paint);
                                break;
                            case Configuration.UI_MODE_NIGHT_YES:
                                //Paints over where the original itemView was with the default background color.
                                paint.setColor(Color.rgb(0,0,0));
                                c.drawRect(itemView.getLeft(), itemView.getTop() , itemView.getRight(), itemView.getBottom(), paint);
                                break;
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }
        };
        //Attaches helper to notification/recent chat recyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewHomeNotifications);
    }

    /**
     * Creates new Connections/Friend Request Notifications for each Request inside
     * requestList from the ConnectionViewModel. If requestlist is empty, makes sure
     * arrayList of HomeFragment is empty of Connection/Friend Request Notifications.
     * Runnable on the inside will check and populate the list every 10 milliseconds.
     *  @param mConnModel ConnectionViewModel, used to grab all connection/friend
     *                    request from the requestList.
     */
    private void setConnNoti(ConnectionsViewModel mConnModel) {
        mConnModel.addRequestListObserver(getViewLifecycleOwner(), requestList -> {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                // Repopulate notificationList with current friend request list.
                if (requestList != null && requestList.size() != 0) {
                    for (int i = 0; i < requestList.size(); i++) {
                        Date date = new Date();
                        Notification connNoti = new Notification.Builder(
                                format.format(date))
                                .addMessage("You have received a new connection request from " + requestList.get(i).getName())
                                .addType("New Connection Request from " + requestList.get(i).getName())
                                .build();
                        mNotiModel.handleNotification(connNoti);
                    }
                }
            }, 10);
        });
    }

    /**
     * Helper function that will take in a weatherInfoModel and get the status
     * for the current day for the current location of the device. With that status,
     * it'll update the home fragment's weather status image as well as the text box
     * that contains the weather status.
     *
     * @param weatherModel WeatherInfoModel use to grab the weather status for the current
     *                     day and device location.
     * @param binding FragmentHomeBinding use for setting the weather status image and text box.
     */
    private void setWeatherStatusImage(WeatherInfoModel weatherModel, FragmentHomeBinding binding) {
        ViewGroup.LayoutParams params = binding.textHomeWeatherCondition.getLayoutParams();
        params.width = dpToPx(144);

        // Sets weather status as well as change the height of the textview so that there won't be a gap
        // between the weather status and the temperature textview.
        switch (weatherModel.getData().getToday().getStatus().toString().toLowerCase().replace("_", " ")) {
            //Case 800: Clear
            case "clear sky":
            case "clear":
                params.height = dpToPx(55);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_clear_sky));
                if (isNight) {
                    binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_clear_sky_night_128dp));
                } else {
                    binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_clear_sky_day_128dp));
                }
                break;

            //Case 801
            case "few clouds":
                params.height = dpToPx(50);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_few_clouds));
                if (isNight) {
                    binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_few_clouds_night_128dp));
                } else {
                    binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_few_clouds_day_128dp));
                }
                break;

            //Case 802
            case "scattered clouds":
                params.height = dpToPx(50);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_scattered_clouds));
                binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_scattered_clouds_128dp));
                break;

            //Case 803/Case 804
            case "broken clouds":
            case "overcast clouds":
                params.height = dpToPx(50);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_broken_clouds));
                binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_broken_clouds_128dp));
                break;

            //Group 3xx/Group 5xx: Drizzle/Rain
            case "light intensity drizzle":
            case "drizzle":
            case "heavy intensity drizzle":
            case "light intensity drizzle rain":
            case "drizzle rain":
            case "heavy intensity drizzle rain":
            case "shower rain and drizzle":
            case "heavy shower rain and drizzle":
            case "shower drizzle":
            case "light intensity shower rain":
            case "shower rain":
            case "heavy intensity shower rain":
            case "ragged shower rain":
                params.height = dpToPx(50);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_shower_rain));
                binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_shower_rain_128dp));
                break;

            //Group 5xx: Rain
            case "rain":
            case "light rain":
            case "moderate rain":
            case "heavy intensity rain":
            case "very heavy rain":
            case "extreme rain":
                params.height = dpToPx(25);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_rain));
                if (isNight) {
                    binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_rain_night_128dp));
                } else {
                    binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_rain_day_128dp));
                }
                break;

            //Group 2xx: Thunderstorms
            case "thunderstorm with light rain":
            case "thunderstorm with rain":
            case "thunderstorm with heavy rain":
            case "light thunderstorm":
            case "thunderstorm":
            case "heavy thunderstorm":
            case "ragged thunderstorm":
            case "thunderstorm with light drizzle":
            case "thunderstorm with drizzle":
            case "thunderstorm with heavy drizzle":
                params.height = dpToPx(25);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_thunderstorm));
                binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_thunderstorm_128dp));
                break;

            //Group 6xx/case 511: Snow/Freezing rain
            case "freezing rain":
            case "light snow":
            case "snow":
            case "heavy snow":
            case "sleet":
            case "light shower sleet":
            case "shower sleet":
            case "light rain and snow":
            case "rain and snow":
            case "light shower snow":
            case "shower snow":
            case "heavy shower snow":
                params.height = dpToPx(25);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_snow));
                binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_snow_128dp));
                break;

            //Group 7xx: Atmosphere
            case "mist":
            case "smoke":
            case "haze":
            case "sand/dust whirls":
            case "fog":
            case "sand":
            case "dust":
            case "volcanic ash":
            case "squalls":
            case "tornado":
                params.height = dpToPx(25);
                binding.textHomeWeatherCondition.setLayoutParams(params);
                binding.textHomeWeatherCondition.setText(getString(R.string.text_mist));
                binding.imageHomeWeatherStatus.setImageIcon(Icon.createWithResource(getContext(), R.drawable.ic_mist_128dp));
                break;

            default:
                binding.imageHomeWeatherStatus.setBackgroundColor(Color.RED);
                Log.e("HomeFragment/",
                        "This weather status does not exist in the switch statement, please double check the spelling of the weather status or create a new case!"
                                + " Create a case for: "
                                + weatherModel.getData().getToday().getStatus().toString().toLowerCase().replace("_", " ")
                                + ".");
        }
    }

    /**
     * Converts dp units into px units.
     *
     * @param dp int length in dp.
     * @return int length in px.
     */
    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts Fahrenheit to Celsius.
     *
     * @param feh Fahrenheit as an int.
     * @return Celsius as an int.
     */
    private int fahToCel(int feh) { return (int) ((feh - 32) * 0.55555555555); }

    @Override
    public void onPause() {
        super.onPause();
        mNotiModel.addNotificationListObserver(getViewLifecycleOwner(), notificationList -> {
            setList(mUserModel.getEmail(), notificationList);
        });
    }

    /**
     * Turns the list of Notifications (Java Object) into a Gson object. Then transforms
     * Gson object into a Json String, ready to be stored in the SharedPreferences.
     *
     * @param key String key connected to the Notification List to store and find.
     *            Always be the email of the User.
     * @param list List of Notifications
     * @param <Notification> Notification object
     */
    public <Notification> void setList(String key, List<Notification> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        set(key, json);
    }

    /**
     * Enters the Json String into the SharedPreferences.
     *
     * @param key String key connected to the Notification List to store and find.
     *            Always be the email of the User.
     * @param value Json String
     */
    public void set(String key, String value) {
        edit.putString(key,value);
        edit.commit();
    }

    /**
     * Grabs the Json String from the SharedPreferences. If not null,
     * converts it into a Gson object then assigned to the list of
     * Notification variable, arr.
     *
     * @param key String key connected to the Notification List to store and find.
     *            Always be the email of the User.
     */
    private void receive(String key) {
        String serial = sp.getString(key,null);
        if (serial != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Notification>>(){}.getType();
            mNotiModel.addNotificationListObserver(getViewLifecycleOwner(), notificationList -> {
                if (notificationList.isEmpty()) {
                    notificationList.addAll(gson.fromJson(serial,type));
                }
            });
        }
    }
}