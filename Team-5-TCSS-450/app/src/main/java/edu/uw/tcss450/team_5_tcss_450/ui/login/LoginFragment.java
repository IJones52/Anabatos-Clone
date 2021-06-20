package edu.uw.tcss450.team_5_tcss_450.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.auth0.android.jwt.JWT;

import org.json.JSONException;
import org.json.JSONObject;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentLoginBinding;
import edu.uw.tcss450.team_5_tcss_450.model.PushyTokenViewModel;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator;

import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkExcludeWhiteSpace;
import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkPwdLength;
import static edu.uw.tcss450.team_5_tcss_450.utils.PasswordValidator.checkPwdSpecialChar;

/**
 * A simple {@link Fragment} subclass.
 * This class provides a function that a user can login with registered email and password.
 *
 * @author Rajdeep Chatha
 * @version May 23, 2021
 */
public class LoginFragment extends Fragment {

    private final PasswordValidator mEmailValidator = checkPwdLength(2)
            .and(checkExcludeWhiteSpace())
            .and(checkPwdSpecialChar("@"));

    private final PasswordValidator mPassWordValidator = checkPwdLength(1)
            .and(checkExcludeWhiteSpace());

    private FragmentLoginBinding binding;

    private LoginViewModel mLoginModel;

    private UserInfoViewModel mUserViewModel;

    private PushyTokenViewModel mPushyTokenViewModel;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoginModel = new ViewModelProvider(getActivity())
                .get(LoginViewModel.class);
        mPushyTokenViewModel = new ViewModelProvider(getActivity())
                .get(PushyTokenViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.register.setOnClickListener(button ->
                Navigation.findNavController(getView()).navigate(
                        LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
                ));

        binding.login.setOnClickListener(this::attemptLogin);

        binding.forgotPassword.setOnClickListener(button ->
                Navigation.findNavController(getView()).navigate(
                        LoginFragmentDirections.actionLoginFragmentToPasswordRecoveryFragment()
                ));

        mLoginModel.addResponseObserver(
                getViewLifecycleOwner(),
                this::observeLoginResponse);

        LoginFragmentArgs args = LoginFragmentArgs.fromBundle(getArguments());

        binding.email.setText(args.getEmail().equals("default") ? "test2@uw.edu" : args.getEmail());
        binding.password.setText(args.getPassword().equals("default") ? "Test12345!" : args.getPassword());

        //don't allow sign in until pushy token retrieved
        mPushyTokenViewModel.addTokenObserver(getViewLifecycleOwner(), token ->
                binding.login.setEnabled(!token.isEmpty()));
        mPushyTokenViewModel.addResponseObserver(
                getViewLifecycleOwner(),
                this::observePushyPutResponse);
    }

    /**
     * Helper to call when login button clicked.
     */
    private void attemptLogin(final View button) {
        validateEmail();
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
                mPassWordValidator.apply(binding.password.getText().toString()),
                this::verifyAuthWithServer,
                result -> binding.password.setError("Please enter a valid Password."));
    }

    /**
     * Helper to verify user email and password with server.
     */
    private void verifyAuthWithServer() {
        mLoginModel.connect(
                binding.email.getText().toString(),
                binding.password.getText().toString());
        //This is an Asynchronous call. No statements after should rely on the
        // result of connect().
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to LoginViewModel.
     *
     * @param response the Response from the server
     */
    private void observePushyPutResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("code")) {
                //this error cannot be fixed by the user changing credentials...
                binding.email.setError(
                        "Error Authenticating on Push Token. Please contact support");
            } else {
                navigateToSuccess(
                        binding.email.getText().toString(),
                        mUserViewModel.getmJwt(),
                        mUserViewModel.getmNick(),
                        mUserViewModel.getmUser(),
                        mUserViewModel.getmId()
                );
            }
        }
    }

    /**
     * Helper to abstract the navigation to the Activity past Authentication.
     *
     * @param email    users email
     * @param jwt      the JSON Web Token supplied by the server
     * @param nickname users nick name
     * @param username user name
     * @param memberid user id
     */
    private void navigateToSuccess(final String email, final String jwt, final String nickname, final String username, final int memberid) {
        if (binding.switchLogin.isChecked()) {
            SharedPreferences prefs =
                    getActivity().getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            //Store the credentials in SharedPrefs
            prefs.edit().putString(getString(R.string.keys_prefs_jwt), jwt).apply();
            prefs.edit().putString(getString(R.string.keys_prefs_email), email).apply();
            prefs.edit().putString(getString(R.string.keys_prefs_nick), nickname).apply();
            prefs.edit().putString(getString(R.string.keys_prefs_user), username).apply();
            prefs.edit().putInt(getString(R.string.keys_prefs_id), memberid).apply();
        }
        Navigation.findNavController(getView())
                .navigate(LoginFragmentDirections
                        .actionLoginFragmentToMainActivity(email, jwt, nickname, username, memberid));
        //Remove THIS activity from the Task list. Pops off the backstack
        getActivity().finish();
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to LoginViewModel.
     *
     * @param response the Response from the server
     */
    private void observeLoginResponse(final JSONObject response) {
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
                try {
                    mUserViewModel = new ViewModelProvider(getActivity(),
                            new UserInfoViewModel.UserInfoViewModelFactory(
                                    binding.email.getText().toString(),
                                    response.getString("token"),
                                    response.getString("nickname"),
                                    response.getString("username"),
                                    response.getInt("memberid")
                            )).get(UserInfoViewModel.class);
                    sendPushyToken();
                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }
            }
        } else {
            Log.d("JSON Response", "No Response");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        if (prefs.contains(getString(R.string.keys_prefs_jwt))) {
            String token = prefs.getString(getString(R.string.keys_prefs_jwt), "");
            JWT jwt = new JWT(token);
            // Check to see if the web token is still valid or not. To make a JWT expire after a
            // longer or shorter time period, change the expiration time when the JWT is
            // created on the web service.
            if (!jwt.isExpired(0)) {
                String email = jwt.getClaim("email").asString();
                String nickname = prefs.getString(getString(R.string.keys_prefs_nick), "");
                String username = prefs.getString(getString(R.string.keys_prefs_user), "");
                int memberid = prefs.getInt(getString(R.string.keys_prefs_id), 0);
                navigateToSuccess(email, token, nickname, username, memberid);
                return;
            }
        }
    }

    /**
     * Helper to abstract the request to send the pushy token to the web service.
     */
    private void sendPushyToken() {
        mPushyTokenViewModel.sendTokenToWebservice(mUserViewModel.getmJwt());
    }
}
