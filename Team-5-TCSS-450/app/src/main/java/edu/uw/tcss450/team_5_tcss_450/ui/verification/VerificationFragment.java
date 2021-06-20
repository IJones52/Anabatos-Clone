package edu.uw.tcss450.team_5_tcss_450.ui.verification;

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

import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentVerificationBinding;

/**
 * A simple {@link Fragment} subclass.
 * This class provides a function that a user can verify the provided information for
 * registration.
 *
 * @author Rajdeep Chatha
 * @version May 23, 2021
 */
public class VerificationFragment extends Fragment {

    private FragmentVerificationBinding binding;

    private VerificationFragmentArgs mArgs;

    private VerificationViewModel mVerificationModel;

    public VerificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVerificationModel = new ViewModelProvider(getActivity())
                .get(VerificationViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVerificationBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.back.setOnClickListener(button ->
                Navigation.findNavController(getView()).navigate(
                        VerificationFragmentDirections.actionVerificationFragmentToRegisterFragment()));

        binding.verification.setOnClickListener(this::attemptVerify);
        mVerificationModel.addResponseObserver(getViewLifecycleOwner(),
                this::observeResponse);

        mArgs = VerificationFragmentArgs.fromBundle(getArguments());

        FragmentVerificationBinding.bind(getView()).nickName.setText(mArgs.getNickName());
        FragmentVerificationBinding.bind(getView()).firstName.setText(mArgs.getFirstName());
        FragmentVerificationBinding.bind(getView()).lastName.setText(mArgs.getLastName());
        FragmentVerificationBinding.bind(getView()).email.setText(mArgs.getEmail());
    }

    /**
     * Helper to call when verification button clicked.
     */
    private void attemptVerify(final View button) {
        mVerificationModel.connect(
                mArgs.getEmail(), mArgs.getPassword());
        //This is an Asynchronous call. No statements after should rely on the
        // result of connect().
    }

    /**
     * Helper to navigate user to login fragment.
     */
    private void navigateToLogin() {
        Navigation.findNavController(getView())
                .navigate(VerificationFragmentDirections
                        .actionVerificationFragmentToLoginFragment());
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
                navigateToLogin();
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }
}
