package edu.uw.tcss450.team_5_tcss_450.ui.password;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentPasswordRecoveryBinding;

/**
 * A simple {@link Fragment} subclass.
 * This class provides a function that a user can recover their password.
 *
 * @author Rajdeep Chatha
 * @version May 23, 2021
 */
public class PasswordRecoveryFragment extends Fragment {

    private FragmentPasswordRecoveryBinding binding;

    private PasswordRecoveryViewModel mPasswordModel;

    public PasswordRecoveryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPasswordModel = new ViewModelProvider(getActivity())
                .get(PasswordRecoveryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordRecoveryBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonCANCEL.setOnClickListener(button ->
                Navigation.findNavController(getView()).navigate(
                        PasswordRecoveryFragmentDirections.actionPasswordRecoveryFragmentToLoginFragment()));

        binding.buttonSEND.setOnClickListener(this::sendEmail);

        mPasswordModel.addResponseObserver(getViewLifecycleOwner(),
                this::observeResponse);
    }

    /**
     * Helper to send an email when send button clicked.
     */
    private void sendEmail(final View button) {
        mPasswordModel.connect(binding.email.getText().toString());
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to SignInViewModel.
     *
     * @param response the Response from the server
     */
    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("code")) {
                try {
                    binding.email.setError(
                            "Error Authenticating: " +
                                    response.getJSONObject("data").getString("message"));
                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }
            } else {
                Log.d("JSON Response", "No Response");
            }
        }
    }
}