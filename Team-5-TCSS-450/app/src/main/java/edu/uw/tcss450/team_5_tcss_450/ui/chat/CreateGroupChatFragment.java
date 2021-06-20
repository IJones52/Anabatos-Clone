package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.os.Bundle;
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

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentCreateGroupChatBinding;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.Connection;
import edu.uw.tcss450.team_5_tcss_450.ui.connections.ConnectionsViewModel;

/**
 * A dialog that allows the user to select the connections they want to create a group chat.
 *
 * @author Khue Nguyen
 * @version 6/5/2021
 */
public class CreateGroupChatFragment extends DialogFragment {
    /**
     * Connection view model.
     */
    ConnectionsViewModel mConnectionModel;

    /**
     * User info view model.
     */
    UserInfoViewModel mUserModel;

    /**
     * Chat list view model.
     */
    ChatListViewModel mChatModel;

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
        mConnectionModel.getConnections(mUserModel.getmId());
        mChatModel = provider.get(ChatListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_group_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentCreateGroupChatBinding binding = FragmentCreateGroupChatBinding.bind(view);

        List<Connection> selectedUsers = new ArrayList<>();
        mConnectionModel.addConnectionListObserver(getViewLifecycleOwner(), list -> {
            binding.recyclerConnections.setAdapter(new CreateGroupChatRecyclerViewAdapter(list,
                    new CreateGroupChatRecyclerViewAdapter.OnUserSelectedListener() {

                        @Override
                        public void onUserSelected(Connection connection) {
                            if (!selectedUsers.contains(connection)) {
                                selectedUsers.add(connection);
                            }
                            Log.d("selectedUsers", selectedUsers.toString());
                        }

                        @Override
                        public void onUserUnselected(Connection connection) {
                            selectedUsers.remove(connection);
                            Log.d("selectedUsers", selectedUsers.toString());
                        }
                    }));
        });

        binding.buttonClose.setOnClickListener(button -> dismiss());
        binding.buttonCreateChat.setOnClickListener(button -> {
            if (selectedUsers.size() < 2) {
                Snackbar.make(view,
                        "Please select at least two users in order to create a group chat.",
                        Snackbar.LENGTH_SHORT).show();
            } else if (binding.editGroupName.getText().toString().trim().length() == 0) {
                Snackbar.make(view, "Please enter a group name.",
                        Snackbar.LENGTH_SHORT).show();
            } else {
                String name = binding.editGroupName.getText().toString();
                mChatModel.createGroupChatRoom(name, selectedUsers, mUserModel.getmJwt());
            }
        });

        mChatModel.addChatListObserver(getViewLifecycleOwner(), this::observeChatList);
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
            Navigation.findNavController(getParentFragment().getView()).navigate(
                    CreateGroupChatFragmentDirections
                            .actionCreateGroupChatFragmentToChatRoomFragment(list.get(0)));
            dismiss();
        }
    }
}
