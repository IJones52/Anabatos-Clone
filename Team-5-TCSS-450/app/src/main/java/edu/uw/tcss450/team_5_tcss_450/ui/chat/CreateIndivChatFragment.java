package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentCreateIndivChatBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.Connection;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.ConnectionsViewModel;

/**
 * A dialog that allows the user to start a chat with one of their connections.
 *
 * @author Khue Nguyen
 * @version 6/5/2021
 */
public class CreateIndivChatFragment extends DialogFragment {
    /**
     * Connection view model.
     */
    private ConnectionsViewModel mConnectionModel;

    /**
     * User info view model.
     */
    private UserInfoViewModel mUserModel;

    /**
     * Chat list view model.
     */
    private ChatListViewModel mChatModel;

    /**
     * Determines whether list is retrieved initially or not.
     */
    private boolean isInit = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme);
        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mConnectionModel = provider.get(ConnectionsViewModel.class);
        mUserModel = provider.get(UserInfoViewModel.class);
        mChatModel = provider.get(ChatListViewModel.class);
        mConnectionModel.getConnections(mUserModel.getmId());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_indiv_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentCreateIndivChatBinding binding = FragmentCreateIndivChatBinding.bind(view);

        List<Connection> selectedUser = new ArrayList<>();
        mConnectionModel.addConnectionListObserver(getViewLifecycleOwner(), list -> {
            binding.recyclerConnections.setAdapter(new CreateIndivChatRecyclerViewAdapter(list,
                    new CreateIndivChatRecyclerViewAdapter.OnUserSelectedListener() {
                        @Override
                        public void onUserSelected(Connection connection) {
                            selectedUser.add(connection);
                        }

                        @Override
                        public void onUserUnselected(Connection connection) {
                            selectedUser.remove(connection);
                        }
                    }));
        });

        binding.buttonClose.setOnClickListener(button -> dismiss());
        binding.buttonCreateChat.setOnClickListener(button -> {
            if (selectedUser.isEmpty()) {
                Snackbar.make(view, "Please select a user in order to create a chat.",
                        Snackbar.LENGTH_SHORT).show();
            } else {
                ChatRoomInfo chatRoomInfo = mChatModel.getChatRoomInfoGivenMembers(selectedUser);
                if (!Objects.isNull(chatRoomInfo)) {
                    Navigation.findNavController(getParentFragment().getView()).navigate(CreateIndivChatFragmentDirections
                            .actionCreateIndivChatFragmentToChatRoomFragment(chatRoomInfo));
                    dismiss();
                } else {
                    mChatModel.createIndivChatRoom(selectedUser, mUserModel.getmJwt());
                }
            }
        });

        mChatModel.addChatListObserver(getViewLifecycleOwner(),this::observeChatList);
    }

    /**
     * Observes changes in the chat list. If chat list is updated initially then don't do anything.
     * Otherwise, navigate to that chat room.
     *
     * @param list the list of chat rooms
     */
    private void observeChatList(List<ChatRoomInfo> list) {
        if (isInit) {
            isInit = false;
            Log.d("observeChatList", "initial list values");
        } else {
            Log.d("observeChatList", "chat list updated: " + list.toString());
            Navigation.findNavController(getParentFragment().getView()).navigate(CreateIndivChatFragmentDirections
                    .actionCreateIndivChatFragmentToChatRoomFragment(list.get(0)));
            dismiss();
        }
    }
}
