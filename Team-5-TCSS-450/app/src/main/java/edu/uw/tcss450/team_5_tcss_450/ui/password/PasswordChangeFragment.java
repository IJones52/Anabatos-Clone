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

import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentPasswordChangeBinding;
import edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator;

import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkClientPredicate;
import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkExcludeWhiteSpace;
import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkPwdDigit;
import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkPwdLength;
import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkPwdLowerCase;
import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkPwdSpecialChar;
import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkPwdUpperCase;

/**
 * A simple {@link Fragment} subclass.
 * This class provides a function that a user can change their password.
 *
 * @author Rajdeep Chatha
 * @version May 23, 2021
 */
public class PasswordChangeFragment extends Fragment {

    private final PasswordValidator mEmailValidator = checkPwdLength(2)
            .and(checkExcludeWhiteSpace())
            .and(checkPwdSpecialChar("@"));

    private FragmentPasswordChangeBinding binding;

    private final PasswordValidator mPassWordValidator =
            checkClientPredicate(pwd -> pwd.equals(binding.newPassword.getText().toString()))
                    .and(checkPwdLength(7))
                    .and(checkPwdSpecialChar())
                    .and(checkExcludeWhiteSpace())
                    .and(checkPwdDigit())
                    .and(checkPwdLowerCase().or(checkPwdUpperCase()));

    private PasswordChangeViewModel mPasswordModel;

    public PasswordChangeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPasswordModel = new ViewModelProvider(getActivity())
                .get(PasswordChangeViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPasswordChangeBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.CANCEL.setOnClickListener(button ->
                Navigation.findNavController(getView()).navigate(
                        PasswordChangeFragmentDirections.actionPasswordChangeFragmentToNavigationSetting()));

        binding.CHANGE.setOnClickListener(this::attemptChange);

        mPasswordModel.addResponseObserver(getViewLifecycleOwner(),
                this::observeResponse);
    }

    /**
     * Helper to call when change button clicked.
     */
    private void attemptChange(final View button) {
        validatePassword();
    }

    /**
     * Helper to validate user password.
     */
    private void validatePassword() {
        mPassWordValidator.processResult(
                mPassWordValidator.apply(binding.oldPassword.getText().toString()),
                this::verifyAuthWithServer,
                result -> binding.oldPassword.setError("Please enter a valid Password."));
    }

    /**
     * Helper to verify user information with server.
     */
    private void verifyAuthWithServer() {
        mPasswordModel.connect(
                //binding.email.getText().toString(),
                binding.oldPassword.getText().toString(),
                binding.newPassword.getText().toString(),
                binding.confirmPassword.getText().toString());
        //This is an Asynchronous call. No statements after should rely on the
        //result of connect().
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to PasswordChangeViewModel.
     *
     * @param response the Response from the server.
     */
    private void observeResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("code")) {
                try {
                    binding.newPassword.setError(
                            "Error Authenticating: " +
                                    response.getJSONObject("data").getString("message"));
                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }
            } else {
                //navigateToLogin();
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }
}