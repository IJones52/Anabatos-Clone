package edu.uw.tcss450.team_5_tcss_450.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class UserInfoViewModel extends ViewModel {

    private final String mEmail;
    private final String mJwt;
    private final String mUserName;
    private final String mNickName;
    private final int mId;


    private UserInfoViewModel(String email, String jwt, String username, String nickname, int memberid) {
        mEmail = email;
        mJwt = jwt;
        mUserName = username;
        mNickName = nickname;
        mId = memberid;

    }

    public String getEmail() {
        return mEmail;
    }

    public String getmJwt() {
        return mJwt;
    }

    public String getmUser() {
        return mUserName;
    }

    public String getmNick() {
        return mNickName;
    }

    public int getmId() {
        return mId;
    }

    public static class UserInfoViewModelFactory implements ViewModelProvider.Factory {

        private final String email;
        private final String jwt;
        private final String username;
        private final String nickname;
        private final int memberId;

        public UserInfoViewModelFactory(String email, String jwt, String username, String nickname, int memberid) {
            this.email = email;
            this.jwt = jwt;
            this.username = username;
            this.nickname = nickname;
            this.memberId = memberid;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass == UserInfoViewModel.class) {
                return (T) new UserInfoViewModel(email, jwt, username, nickname, memberId);
            }
            throw new IllegalArgumentException(
                    "Argument must be: " + UserInfoViewModel.class);
        }
    }
}