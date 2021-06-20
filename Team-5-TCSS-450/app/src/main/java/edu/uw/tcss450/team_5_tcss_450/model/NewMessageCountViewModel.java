package edu.uw.tcss450.team_5_tcss_450.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.Map;

public class NewMessageCountViewModel extends ViewModel {
    private MutableLiveData<Integer> mTotalMsgs;
    private Map<Integer, MutableLiveData<Integer>> mMsgPerChat;

    public NewMessageCountViewModel() {
        mMsgPerChat = new HashMap<>();
        mTotalMsgs = new MutableLiveData<>();
        mTotalMsgs.setValue(0);
    }

    private MutableLiveData<Integer> getOrCreateEntry(int chatid) {
        if (!mMsgPerChat.containsKey(chatid)) {
            mMsgPerChat.put(chatid, new MutableLiveData<>());
            mMsgPerChat.get(chatid).setValue(0);
        }
        return mMsgPerChat.get(chatid);
    }

    public void addTotalMsgCountObserver(@NonNull LifecycleOwner owner, @NonNull Observer<? super Integer> observer) {
        mTotalMsgs.observe(owner, observer);
    }

    public void addMessageCountObserver(int chatid, @NonNull LifecycleOwner owner, @NonNull Observer<? super Integer> observer) {
        getOrCreateEntry(chatid).observe(owner, observer);
    }

    public void increment(int chatid) {
        mTotalMsgs.setValue(mTotalMsgs.getValue() + 1);
        getOrCreateEntry(chatid).setValue(getOrCreateEntry(chatid).getValue() + 1);
    }

    public void reset(int chatid) {
        mTotalMsgs.setValue(mTotalMsgs.getValue() - getOrCreateEntry(chatid).getValue());
        getOrCreateEntry(chatid).setValue(0);
    }
}
