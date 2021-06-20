package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Class to encapsulate a message from a chatroom. Messages can either have type 1 (from user) or
 * type 2 (from connection).
 *
 * @author Khue Nguyen
 * @version 4/30/2021
 */
public class ChatMessage implements Serializable {
    /**
     * The message contents.
     */
    private final String mMessage;

    /**
     * The message id.
     */
    private final int mMessageId;

    /**
     * The message timestamp (date and time)
     */
    private final String mTimeStamp;

    /**
     * The name of the sender.
     */
    private final String mSender;

    /**
     * The email of the sender.
     */
    private final String mEmail;

    /**
     * Creates an instance of a chat message.
     * @param messageId the id
     * @param message the contents
     * @param sender the sender's name
     * @param senderEmail the sender's email
     * @param timeStamp the timestamp
     */
    public ChatMessage(int messageId, String message, String sender, String senderEmail, String timeStamp) {
        mMessage = message;
        mMessageId = messageId;
        mTimeStamp = timeStamp;
        mSender = sender;
        mEmail = senderEmail;
    }

    /**
     * Static factory method to turn a properly formatted JSON String into a ChatMessage object.
     *
     * @param msgAsJson the String to be parsed into a ChatMessage object.
     * @return a ChatMessage object with the details contained in the JSON String
     * @throws JSONException when msgAsJson cannot be parsed into a ChatMessage.
     */
    public static ChatMessage createFromJsonString(final String msgAsJson) throws JSONException {
        final JSONObject msg = new JSONObject(msgAsJson);
        final String name = msg.getString("firstname") + " " + msg.getString("lastname");
        return new ChatMessage(msg.getInt("messageid"),
                msg.getString("message"),
                name,
                msg.getString("email"),
                msg.getString("timestamp"));
    }

    /**
     * Get the sender's email of a message
     * @return sender's email
     */
    public String getEmail() {
        return mEmail;
    }

    /**
     * Get the contents of a message
     * @return message's content
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * Get the message id
     * @return message id
     */
    public int getMessageId() {
        return mMessageId;
    }

    /**
     * Get the message timestamp
     * @return message timestamp
     */
    public String getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Get the message sender's name
     * @return sender's name
     */
    public String getSender() {
        return mSender;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        boolean result = false;
        if (o instanceof ChatMessage) {
            result = mMessageId == ((ChatMessage) o).mMessageId;
        }

        return result;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "mMessage='" + mMessage + '\'' +
                ", mEmail='" + mEmail + '\'' +
                '}';
    }
}
