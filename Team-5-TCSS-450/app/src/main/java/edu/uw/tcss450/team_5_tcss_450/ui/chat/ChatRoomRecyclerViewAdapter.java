package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Timestamp;
import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentChatMessageReceiverBinding;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentChatMessageSenderBinding;


/**
 * A RecyclerViewAdapter that displays the history of messages within a chatroom.
 * @author Khue Nguyen
 * @version 6/5/2021
 */
 public class ChatRoomRecyclerViewAdapter extends RecyclerView.Adapter {
    /**
     * List of messages.
     */
    private final List<ChatMessage> mMessages;

    /**
     * The email to display.
     */
    private final String mEmail;

    /**
     * A static field that marks messages sent from sender to determine the view to inflate.
     */
    public static final int MSG_TYPE_SENDER = 1;

    /**
     * A static field that marks messages sent from receiver to determine the view to inflate.
     */
    public static final int MSG_TYPE_RECEIVER = 2;

    /**
     * Creates an instance of the chat message list adapter
     * @param messages the list of messages
     * @param email the user's email
     */
    public ChatRoomRecyclerViewAdapter(List<ChatMessage> messages, String email) {
        mMessages = messages;
        mEmail = email;
    }

     @Override
     public int getItemViewType(int position) {
         if (mEmail.equals(mMessages.get(position).getEmail())) {
             return MSG_TYPE_SENDER;
         }
         return MSG_TYPE_RECEIVER;
     }

     @NonNull
     @Override
     public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         if (viewType == MSG_TYPE_SENDER) {
             return new SenderMessageViewHolder(LayoutInflater.from(parent.getContext())
                     .inflate(R.layout.fragment_chat_message_sender, parent, false));
         } else {
             return new ReceiverMessageViewHolder(LayoutInflater.from(parent.getContext())
                     .inflate(R.layout.fragment_chat_message_receiver, parent, false));
         }
     }

     @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage currMsg = mMessages.get(position);
        ChatMessage prevMsg = position > 0 ? mMessages.get(position - 1) : null;

        switch (holder.getItemViewType()) {
            case MSG_TYPE_SENDER:
                ((SenderMessageViewHolder) holder).setMessage(currMsg, handleDate(currMsg, prevMsg));
                break;
            case MSG_TYPE_RECEIVER:
                ((ReceiverMessageViewHolder) holder).setMessage(currMsg, handleDate(currMsg, prevMsg));
        }

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    /**
     * A private method that calculates the amount of days in between two messages. If the
     * amount of days is >= 1 day, then the date label will be displayed.
     *
     * @param currMsg The current message in the list
     * @param prevMsg The previous message in the list
     * @return a boolean that determines whether or not the date label should be displayed
     */
    private boolean handleDate(ChatMessage currMsg, ChatMessage prevMsg) {
        // Factor of converting milliseconds to days
        final int ms_to_days = 1000*60*60*24;

        if (prevMsg == null) {
            return true;
        }

        boolean result = false;
        String s1 = currMsg.getTimeStamp();
        String s2 = prevMsg.getTimeStamp();
        Timestamp t1 = Timestamp.valueOf(s1);
        Timestamp t2 = Timestamp.valueOf(s2);
        long diff = t1.getTime() - t2.getTime();

        if (diff/ms_to_days > 1) {
            result = true;
        }
        return result;
    }



    /**
     * Objects from this class represent the view of messages sent by the user (aka sender).
     */
    private class SenderMessageViewHolder extends RecyclerView.ViewHolder {
        /**
         * The binding of view for messages sent be sender.
         */
        private FragmentChatMessageSenderBinding mBinding;

        /**
         * Creates an instance of the view holder.
         * @param view the view for sender's message
         */
        SenderMessageViewHolder(final View view) {
            super(view);
            mBinding = FragmentChatMessageSenderBinding.bind(view);
        }

        /**
         * Sets the message view.
         * @param message the message to be displayed
         * @param isDifferentDay boolean to display the date label
         */
        void setMessage(final ChatMessage message, boolean isDifferentDay) {
            mBinding.textMessage.setText(message.getMessage());
            String[] dateAndTime = message.getTimeStamp().split(" ");
            // set time in HH:mm format
            mBinding.textTime.setText(dateAndTime[1].substring(0, 5));
            if (isDifferentDay) {
                mBinding.textDate.setText(dateAndTime[0]);
                mBinding.textDate.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Objects from this class represent the view of messages sent by the recipient (aka receiver).
     */
    private class ReceiverMessageViewHolder extends RecyclerView.ViewHolder {
        /**
         * The binding of view for messages sent be receiver.
         */
        private FragmentChatMessageReceiverBinding mBinding;

        /**
         * Creates an instance of the view holder.
         * @param view the view for receiver's message
         */
        ReceiverMessageViewHolder(final View view) {
            super(view);
            mBinding = FragmentChatMessageReceiverBinding.bind(view);
        }

        /**
         * Sets the message view.
         * @param message the message to be displayed
         * @param isDifferentDay boolean to display the date label
         */
        void setMessage(final ChatMessage message, boolean isDifferentDay) {
            mBinding.textSenderName.setText(message.getSender());
            mBinding.textMessage.setText(message.getMessage());
            String[] dateAndTime = message.getTimeStamp().split(" ");
            // set time in HH:mm format
            mBinding.textTime.setText(dateAndTime[1].substring(0, 5));
            if (isDifferentDay) {
                mBinding.textDate.setText(dateAndTime[0]);
            }
        }
    }

}
