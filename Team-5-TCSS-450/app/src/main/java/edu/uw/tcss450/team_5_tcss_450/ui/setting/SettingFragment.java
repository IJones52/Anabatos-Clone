package edu.uw.tcss450.team_5_tcss_450.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentSettingBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;

/**
 * A simple {@link Fragment} subclass.
 * This class provides a function that a user can change the day and night mode
 * theme and go to change their password.
 *
 * @author Rajdeep Chatha
 * @version May 23, 2021
 */
public class SettingFragment extends Fragment {

    private FragmentSettingBinding binding;

    private UserInfoViewModel mUserModel;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.changePassword.setOnClickListener(button ->
                Navigation.findNavController(getView()).navigate(
                        SettingFragmentDirections.actionNavigationSettingToPasswordChangeFragment()));
        SharedPreferences preferences = getActivity().getSharedPreferences("Theme", Context.MODE_PRIVATE);


        binding.switchMode.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        preferences.edit().putBoolean("Night Mode", true).apply();
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        getActivity().setTheme(R.style.Theme_NightMode);

                    } else {
                        preferences.edit().putBoolean("Night Mode", false).apply();
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        getActivity().setTheme(R.style.Theme_DayMode);
                    }
                }
        );
    }

}