package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

/**
 * Keeps track of the chat id of the chat room that's being displayed, that is, if the user is
 * currently on the ChatRoomFragment.
 * @author Khue Nguyen
 * @version 6/5/2021
 */
public class ChatIdViewModel extends ViewModel {

    /**
     * The chat id of the current chat room being displayed.
     */
    private final MutableLiveData<Integer> mChatId;

    /**
     * Creates a new instance of the model.
     * Instantiate necessary variables.
     */
    public ChatIdViewModel() {
        mChatId = new MutableLiveData<>();
    }

    /**
     * Observe any changes to mChatId
     * @param owner the life cycle owner
     * @param observer the observer
     */
    public void addChatIdObserver(@NonNull LifecycleOwner owner, Observer<? super Integer> observer) {
        mChatId.observe(owner, observer);
    }

    /**
     * Updates the chat id when the user navigates to a new chat room.
     * @param id
     */
    public void updateChatId(int id) {
        mChatId.setValue(id);
    }
}
