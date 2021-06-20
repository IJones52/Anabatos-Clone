package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentChatRoomBinding;
import edu.uw.tcss450.team_5_tcss_450.model.NewMessageCountViewModel;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.notifications.Notification;
import edu.uw.tcss450.team_5_tcss_450.notifications.NotificationListViewModel;

/**
 * A fragment that displays the views of a chatroom. That is, all the messages sent between the
 * users and a place to type in the messages.
 * @author Khue Nguyen
 * @version 6/5/2021
 */
public class ChatRoomFragment extends Fragment {
    /** The view model for the user info. */
    private UserInfoViewModel mUserModel;

    /** The view model for the chat. */
    private ChatViewModel mChatModel;

    /** The view model for the chat sending requests. */
    private ChatSendViewModel mSendModel;

    /** Arguments passed to this fragment. */
    private ChatRoomFragmentArgs mArgs;
    
    /** The binding associated with the fragment. */
    private FragmentChatRoomBinding mBinding;

    /** The chat id view model. */
    private ChatIdViewModel mChatIdViewModel;
    
    /** Notification ViewModel variable */
    private NotificationListViewModel mNotiModel; 

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instantiate args
        mArgs = ChatRoomFragmentArgs.fromBundle(getArguments());

        // Instantiate view models
        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mUserModel = provider.get(UserInfoViewModel.class);
        mChatModel = provider.get(ChatViewModel.class);
        mSendModel = provider.get(ChatSendViewModel.class);
        mChatIdViewModel = provider.get(ChatIdViewModel.class);
        mNotiModel = provider.get(NotificationListViewModel.class);

        // get first messages
        mChatModel.getFirstMessages(mArgs.getChatRoom().getmChatId(), mUserModel.getmJwt());

        // set chat id
        mChatIdViewModel.updateChatId(mArgs.getChatRoom().getmChatId());

        //Creates a recent chat Notification
        //Date should be last sent message
        /*Notification recentChatNoti = new Notification.Builder(mArgs.getChatRoom().getmRecentMsg().getTimeStamp())
                .addMessage(mArgs.getChatRoom().getmRecentMsg().getMessage())
                .addType("Recent Chat with " + mArgs.getChatRoom().getmChatName())
                .build();
        mNotiModel.handleNotification(recentChatNoti);*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding = FragmentChatRoomBinding.bind(view);
        mBinding.swipeContainer.setRefreshing(true);

        //Delete new message notification connected to this chatroom (if applicable)
        mNotiModel.addNotificationListObserver(getViewLifecycleOwner(), notificationList -> {
            for(int i = 0; i < notificationList.size(); i++) {
                Notification temp = notificationList.get(i);
                if (temp.getType().toLowerCase().contains("new message")) {
                    if(temp.getType().toLowerCase().contains(mArgs.getChatRoom().getmChatName())) {
                        notificationList.remove(i);
                        break;
                    }
                }
            }
        });

        // Setting LayoutManager for RecyclerView
        final RecyclerView rv = mBinding.messageListRoot;

        // Set adapter for RecyclerView
        rv.setAdapter(new ChatRoomRecyclerViewAdapter(
                mChatModel.getMessageListByChatId(mArgs.getChatRoom().getmChatId()), mUserModel.getEmail()));

        // Swiper list refresh
        mBinding.swipeContainer.setOnRefreshListener(() -> {
            mChatModel.getNextMessages(mArgs.getChatRoom().getmChatId(), mUserModel.getmJwt());
        });


        // Add onClickListener to send button
        mBinding.buttonSend.setOnClickListener(button -> {
            mSendModel.sendMessage(mArgs.getChatRoom().getmChatId(), mUserModel.getmJwt(),
                    mBinding.editMessage.getText().toString());
        });

        // Add observer to SendModel
        mSendModel.addResponseObserver(getViewLifecycleOwner(),
                response -> mBinding.editMessage.setText("")); // when message is sent, clear text box

        // Add observer to ChatModel
        mChatModel.addMessageObserver(mArgs.getChatRoom().getmChatId(), getViewLifecycleOwner(), list -> {
            Log.d("ChatRoom Fragment", list.toString());
            rv.getAdapter().notifyDataSetChanged();
            rv.scrollToPosition(rv.getAdapter().getItemCount()-1);
            mBinding.swipeContainer.setRefreshing(false);
        });

        // set title for action bar
        if (mArgs.getChatRoom().getmChatMembers().size() > 1) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mArgs.getChatRoom().getmChatName());
        } else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mArgs.getChatRoom().getmChatMembers().get(0).getName());
        }
    }
}
