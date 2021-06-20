package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentChatListBinding;
import edu.uw.tcss450.team_5_tcss_450.model.NewMessageCountViewModel;
import edu.uw.tcss450.team_5_tcss_450.model.UserInfoViewModel;

/**
 * A fragment that displays a list of existing chatrooms in a Recyclerview.
 *
 * @author Khue Nguyen
 * @version 5/19/2021
 */
public class ChatListFragment extends Fragment {
    /**
     * The view model for the user info.
     */
    private UserInfoViewModel mUserModel;

    /**
     * The view model for the chat list.
     */
    private ChatListViewModel mChatListModel;

    /**
     * The binding associated with the fragment.
     */
    private FragmentChatListBinding mBinding;

    /**
     * The view model that keeps track of the counts of new messages for each chat room.
     */
    private NewMessageCountViewModel mMsgCountModel;

    /**
     * List of chat room information.
     */
    private List<ChatRoomInfo> mChatInfo;

    /**
     * The adapter for the list of chats RecyclerView.
     */
    private ChatListRecyclerViewAdapter mAdapter;

    /**
     * The animation for rotating the buttons when opening.
     */
    private Animation mRotateOpen;

    /**
     * The animation for rotating the buttons when closing.
     */
    private Animation mRotateClose;

    /**
     * The animation for translating the buttons when opening.
     */
    private Animation mFromBottom;

    /**
     * The animation for translating the buttons when closing.
     */
    private Animation mToBottom;

    /**
     * The boolean that determines if the mini FABs should be displayed or not.
     */
    private boolean mOpened;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // instantiate view models
        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mUserModel = provider.get(UserInfoViewModel.class);
        mChatListModel = provider.get(ChatListViewModel.class);
        mChatListModel.init(mUserModel.getmJwt());
        mMsgCountModel = provider.get(NewMessageCountViewModel.class);

        // instantiate animations
        mRotateOpen = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_open_anim);
        mRotateClose = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_close_anim);
        mFromBottom = AnimationUtils.loadAnimation(getContext(), R.anim.from_bottom_anim);
        mToBottom = AnimationUtils.loadAnimation(getContext(), R.anim.to_bottom_anim);
        mOpened = false;
        mChatInfo = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding = FragmentChatListBinding.bind(view);

        final RecyclerView rv = mBinding.chatListRoot;

        // set layout manager
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));

        Map<ChatRoomInfo, Integer> counts = new HashMap<>();
        mChatListModel.addChatListObserver(getViewLifecycleOwner(), chatList -> {
            if (!chatList.isEmpty()) {
                mChatInfo = chatList;

                for (int i = 0; i < chatList.size(); i++) {
                    ChatRoomInfo c = chatList.get(i);
                    mMsgCountModel.addMessageCountObserver(c.getmChatId(), getViewLifecycleOwner(), count -> {
                        counts.put(c, count);
                        Log.d("Count", String.valueOf(count));
                    });
                }

                mAdapter = new ChatListRecyclerViewAdapter(chatList, counts,
                        new ChatListRecyclerViewAdapter.OnChatRoomClickListener() {
                            @Override
                            public void onChatRoomClick(int chatId) {
                                mMsgCountModel.reset(chatId);
                            }
                        });
                mBinding.chatListRoot.setAdapter(mAdapter);
            }
        });

        final FloatingActionButton create_btn = mBinding.fabCreateChat;
        final FloatingActionButton indiv_btn = mBinding.fabCreateIndivChat;
        final FloatingActionButton group_btn = mBinding.fabCreateGroupChat;
        create_btn.setOnClickListener(button -> {
            if (!mOpened) {
                group_btn.setVisibility(View.VISIBLE);
                indiv_btn.setVisibility(View.VISIBLE);
                group_btn.setClickable(true);
                indiv_btn.setClickable(true);
                group_btn.startAnimation(mFromBottom);
                indiv_btn.startAnimation(mFromBottom);
                create_btn.startAnimation(mRotateOpen);
            } else {
                group_btn.setVisibility(View.INVISIBLE);
                indiv_btn.setVisibility(View.INVISIBLE);
                group_btn.setClickable(false);
                indiv_btn.setClickable(false);
                group_btn.startAnimation(mToBottom);
                indiv_btn.startAnimation(mToBottom);
                create_btn.startAnimation(mRotateClose);
            }
            mOpened = !mOpened;
        });

        group_btn.setOnClickListener(button -> {
            Navigation.findNavController(getView()).navigate(
                    ChatListFragmentDirections.actionNavigationChatToCreateGroupChatFragment());
        });

        indiv_btn.setOnClickListener(button -> {
            Navigation.findNavController(getView()).navigate(
                    ChatListFragmentDirections.actionNavigationChatToCreateIndivChatFragment());
        });

    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        ChatRoomInfo chatRoomInfo = mChatInfo.get(mAdapter.getPosition());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        switch (item.getItemId()) {
            case 0: // add users
                // Todo pop up dialog to add users
                break;
            case 1: // remove users
                // Todo pop up dialog to remove users
                break;
            case 2: // leave chat
                // Todo pop up dialog to confirm
                builder.setMessage("Do you want to leave this chat?").setCancelable(false)
                        .setPositiveButton("Yes", ((dialog, which) -> {
                            mChatListModel.leaveChat(chatRoomInfo, mUserModel.getmId(),
                                    mUserModel.getmJwt());
                            Snackbar.make(getView(), "You have left the chat room selected.",
                                    Snackbar.LENGTH_SHORT).show();
                        })).setNegativeButton("No", (dialog, which) -> {
                    dialog.cancel();

                });
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case 3: // delete chat
                // Todo pop up dialog to confirm
                builder.setMessage("Do you want to delete this chat?").setCancelable(false)
                        .setPositiveButton("Yes", ((dialog, which) -> {
                            mChatListModel.deleteChat(chatRoomInfo, mUserModel.getmJwt());
                            Snackbar.make(getView(), "The chat room selected has been deleted.",
                                    Snackbar.LENGTH_SHORT).show();
                        })).setNegativeButton("No", (dialog, which) -> {
                    dialog.cancel();
                });
                alert = builder.create();
                alert.show();
                break;
        }
        return super.onContextItemSelected(item);
    }
}