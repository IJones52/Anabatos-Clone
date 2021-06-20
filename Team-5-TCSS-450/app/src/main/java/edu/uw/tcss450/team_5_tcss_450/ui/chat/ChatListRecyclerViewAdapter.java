package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Timestamp;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentChatCardBinding;
import edu.uw.tcss450.team_5_tcss_450.model.NewMessageCountViewModel;

/**
 * A RecyclerViewAdapter that displays a list of existing chatrooms. Each chatrooms are sorted in
 * the order of most recent message of each chatroom.
 *
 * @author Khue Nguyen
 * @version 6/5/2021
 */
public class ChatListRecyclerViewAdapter extends
        RecyclerView.Adapter<ChatListRecyclerViewAdapter.ChatListViewHolder> {
    /**
     * List of chats.
     */
    private final List<ChatRoomInfo> mChats;

    /**
     * Counts of new messages for each chat rooms.
     */
    private final Map<ChatRoomInfo, Integer> mCounts;

    /**
     * The position of the clicked view.
     */
    private int mPosition;

    /**
     * An instance of a listener that listens for on clicked events.
     */
    private OnChatRoomClickListener mListener;

    /**
     * A listening interface that listens for on clicked events.
     */
    interface OnChatRoomClickListener {
        public void onChatRoomClick(int chatId);
    }

    /**
     * Instantiates a new ChatListRecyclerViewAdapter object. Sorts the given list of chatrooms in
     * order of most recent messages.
     *
     * @param chatRoomInfos list of existing chatrooms
     */
    public ChatListRecyclerViewAdapter(List<ChatRoomInfo> chatRoomInfos, Map<ChatRoomInfo, Integer> counts, @NonNull OnChatRoomClickListener clickListener) {
        mChats = chatRoomInfos;
        mCounts = counts;
        mListener = clickListener;
    }

    /**
     * Gets the position of the clicked view.
     * @return position of the clicked view.
     */
    public int getPosition() {
        return mPosition;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_chat_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {
        holder.setPreview(mChats.get(position), mCounts.get(mChats.get(position)));
        holder.chatBinding.layoutInner.setOnLongClickListener(card -> {
            mPosition = position;
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return mChats.size();
    }

    /**
     * An inner class that represents the individual views of each list item within the
     * Recyclerview. Each list items represent an existing chatroom.
     */
    public class ChatListViewHolder extends RecyclerView.ViewHolder implements
            View.OnCreateContextMenuListener {
        /**
         * The current view of the item.
         */
        public final View mView;

        /**
         * The chat room.
         */
        private ChatRoomInfo mChatRoomInfo;

        /**
         * The binding of the view.
         */
        private FragmentChatCardBinding chatBinding;

        /**
         * Instantiates the ChatViewHolder object.
         *
         * @param view the view of the existing chatroom
         */
        public ChatListViewHolder(View view) {
            super(view);
            mView = view;
            chatBinding = FragmentChatCardBinding.bind(mView);
        }

        /**
         * A private method that calculates the amount of days in between two messages. If the
         * amount of days is >= 1 day, then the date will be displayed. Otherwise, the time will be
         * displayed.
         *
         * @param currMsg The current message in the list
         * @return true if date should be displayed, false if time should be displayed
         */
        private boolean handleDate(ChatMessage currMsg) {
            // Factor of converting milliseconds to days
            final int ms_to_days = 1000 * 60 * 60 * 24;

            boolean result = false;

            String s1 = currMsg.getTimeStamp();
            Timestamp t1 = Timestamp.valueOf(s1);
            Timestamp t2 = new Timestamp(new Date().getTime());
            long diff = t1.getTime() - t2.getTime();

            if (diff / ms_to_days > 1) {
                result = true;
            }
            return result;
        }

        /**
         * A method that sets the view with the corresponding information of the given chatroom.
         *
         * @param chatRoomInfo the existing chatroom
         */
        void setPreview(final ChatRoomInfo chatRoomInfo, int count) {
            // set context menu
            mChatRoomInfo = chatRoomInfo;
            chatBinding.layoutInner.setOnCreateContextMenuListener(this);

            // set notification
            if (count > 0) {
                chatBinding.countContainer.setVisibility(View.VISIBLE);
                chatBinding.textCount.setText(String.valueOf(count));
            } else {
                chatBinding.countContainer.setVisibility(View.INVISIBLE);
            }

            // if card is clicked, navigate to the chat room
            chatBinding.layoutInner.setOnClickListener(view -> {
                mListener.onChatRoomClick(chatRoomInfo.getmChatId());
                Navigation.findNavController(mView).navigate(
                        ChatListFragmentDirections.actionChatListFragmentToChatRoomFragment(
                                chatRoomInfo));
            });

            // set individual or group chat icon
            if (chatRoomInfo.getmChatMembers().size() > 1) {
                chatBinding.textName.setText(chatRoomInfo.getmChatName());
                chatBinding.imageUserIcon.setImageResource(R.drawable.ic_group_grey_24dp);
            } else {
                chatBinding.textName.setText(chatRoomInfo.getmChatMembers().get(0).getName());
            }

            // if the chat room has a recent msg, display it
            if (!Objects.isNull(chatRoomInfo.getmRecentMsg())) {
                ChatMessage recentMsg = chatRoomInfo.getmRecentMsg();
                String recentMsgContent = recentMsg.getMessage();
                recentMsgContent = (recentMsgContent
                        .length() < 80) ? recentMsgContent : recentMsgContent
                        .substring(0, 80) + "...";
                chatBinding.textPreview.setText(recentMsgContent);

                String[] timestamp = recentMsg.getTimeStamp().split(" ");
                if (handleDate(recentMsg)) {
                    chatBinding.textDate.setText(timestamp[0]);
                } else {
                    chatBinding.textDate.setText(timestamp[1].substring(0, 5));
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (mChatRoomInfo.getmChatMembers().size() > 1) {
                menu.add(getAdapterPosition(), 0, 0, "Add users");
                menu.add(getAdapterPosition(), 1, 1, "Remove users");
                menu.add(getAdapterPosition(), 2, 2, "Leave chat");
                menu.add(getAdapterPosition(), 3, 3, "Delete chat");
            } else {
                menu.add(getAdapterPosition(), 2, 0, "Leave chat");
                menu.add(getAdapterPosition(), 3, 1, "Delete chat");
            }
        }
    }
}
