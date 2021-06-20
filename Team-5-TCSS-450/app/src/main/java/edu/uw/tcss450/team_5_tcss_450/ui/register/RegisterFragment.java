package edu.uw.tcss450.team_5_tcss_450.ui.register;

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

import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentRegisterBinding;
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
 * This class provides a function that a user can register with their nickname,
 * firstname, lastname, email and password.
 *
 * @author Rajdeep Chatha
 * @version May 23, 2021
 */
public class RegisterFragment extends Fragment {

    private final PasswordValidator mNameValidator = checkPwdLength(1);

    private final PasswordValidator mEmailValidator = checkPwdLength(2)
            .and(checkExcludeWhiteSpace())
            .and(checkPwdSpecialChar("@"));

    private FragmentRegisterBinding binding;

    private final PasswordValidator mPassWordValidator =
            checkClientPredicate(pwd -> pwd.equals(binding.retypePassword.getText().toString()))
                    .and(checkPwdLength(7))
                    .and(checkPwdSpecialChar())
                    .and(checkExcludeWhiteSpace())
                    .and(checkPwdDigit())
                    .and(checkPwdLowerCase().or(checkPwdUpperCase()));

    private RegisterViewModel mRegisterModel;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRegisterModel = new ViewModelProvider(getActivity())
                .get(RegisterViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.register.setOnClickListener(this::attemptRegister);
        mRegisterModel.addResponseObserver(getViewLifecycleOwner(),
                this::observeResponse);
    }

    /**
     * Helper to call when register button clicked.
     */
    private void attemptRegister(final View button) {
        validateNick();
    }

    /**
     * Helper to validate user nick name.
     */
    private void validateNick() {
        mNameValidator.processResult(
                mNameValidator.apply(binding.nickName.getText().toString().trim()),
                this::validateFirst,
                result -> binding.nickName.setError("Please enter a Nick Name."));
    }

    /**
     * Helper to validate user first name.
     */
    private void validateFirst() {
        mNameValidator.processResult(
                mNameValidator.apply(binding.firstName.getText().toString().trim()),
                this::validateLast,
                result -> binding.firstName.setError("Please enter a First Name."));
    }

    /**
     * Helper to validate user last name.
     */
    private void validateLast() {
        mNameValidator.processResult(
                mNameValidator.apply(binding.lastName.getText().toString().trim()),
                this::validateEmail,
                result -> binding.lastName.setError("Please enter a Last Name."));
    }

    /**
     * Helper to validate user email.
     */
    private void validateEmail() {
        mEmailValidator.processResult(
                mEmailValidator.apply(binding.email.getText().toString().trim()),
                this::validatePassword,
                result -> binding.email.setError("Please enter a valid Email address."));
    }

    /**
     * Helper to validate user password.
     */
    private void validatePassword() {
        mPassWordValidator.processResult(
                mPassWordValidator.apply(binding.registerPassword.getText().toString()),
                this::verifyAuthWithServer,
                result -> binding.registerPassword.setError("Please enter a valid Password."));
    }

    /**
     * Helper to verify user information with server.
     */
    private void verifyAuthWithServer() {
        mRegisterModel.connect(
                binding.nickName.getText().toString(),
                binding.firstName.getText().toString(),
                binding.lastName.getText().toString(),
                binding.email.getText().toString(),
                binding.registerPassword.getText().toString());
        //This is an Asynchronous call. No statements after should rely on the
        // result of connect().
    }

    /**
     * Helper to navigate user information for verification.
     */
    private void navigateToVerify() {
        RegisterFragmentDirections.ActionRegisterFragmentToVerificationFragment directions =
                RegisterFragmentDirections.actionRegisterFragmentToVerificationFragment(
                        binding.nickName.getText().toString(),
                        binding.firstName.getText().toString(),
                        binding.lastName.getText().toString(),
                        binding.email.getText().toString(),
                        binding.registerPassword.getText().toString());

        Navigation.findNavController(getView()).navigate(directions);
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to RegisterViewModel.
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
                navigateToVerify();
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }
}