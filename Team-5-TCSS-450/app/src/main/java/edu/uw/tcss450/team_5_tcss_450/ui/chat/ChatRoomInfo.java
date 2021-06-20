package edu.uw.tcss450.team_5_tcss_450.ui.chat;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import edu.uw.tcss450.team_5_tcss_450.ui.connections.Connection;

/**
 * Contains all related information in a ChatRoom.
 *
 * @author Khue Nguyen
 * @version 4/30/2021
 */
public class ChatRoomInfo implements Serializable {
    /**
     * The chat id.
     */
    private final int mChatId;

    /**
     * The chat room name.
     */
    private final String mChatName;

    /**
     * The members of the chat.
     */
    private final List<Connection> mChatMembers;

    /**
     * The most recent message of the chat.
     */
    private final ChatMessage mRecentMsg;

    /**
     * A static class that builds an instance of a ChatRoomInfo object.
     */
    public static class Builder {
        /**
         * The chat id.
         */
        private final int mChatId;

        /**
         * The chat room name.
         */
        private final String mChatName;

        /**
         * The members of the chat.
         */
        private List<Connection> mChatMembers = new ArrayList<>();

        /**
         * The most recent message of the chat.
         */
        private ChatMessage mRecentMsg = null;

        /**
         * A Builder constructor that takes the necessary components of a ChatRoomInfo object.
         * @param id chat id.
         * @param name chat name.
         */
        public Builder(int id, String name) {
            this.mChatId = id;
            this.mChatName = name;
        }

        /**
         * Optional method that adds the members to the ChatRoomInfo object.
         * @param members list of chat members to add
         * @return Builder object
         */
        public Builder addMembers(final List<Connection> members) {
            this.mChatMembers = members;
            return this;
        }

        /**
         * Optional method that adds the member to the ChatRoomInfo object.
         * @param member the chat member to add
         * @return Builder object
         */
        public Builder addMember(final Connection member) {
            this.mChatMembers.add(member);
            return this;
        }

        /**
         * Optional method that adds the most recent chat message to the ChatRoomInfo object.
         * @param msg the recent message to add
         * @return Builder object
         */
        public Builder addRecentMsg(final ChatMessage msg) {
            this.mRecentMsg = msg;
            return this;
        }

        /**
         * Builds a new ChatRoomInfo object.
         * @return a ChatRoomInfo object.
         */
        public ChatRoomInfo build() {
            return new ChatRoomInfo(this);
        }
    }

    /**
     * Private constructor
     * @param builder the Builder object
     */
    private ChatRoomInfo(final Builder builder) {
        this.mChatId = builder.mChatId;
        this.mChatMembers = builder.mChatMembers;
        this.mChatName = builder.mChatName;
        this.mRecentMsg = builder.mRecentMsg;
    }

    /**
     * Gets chat id.
     * @return chat id
     */
    public int getmChatId() {
        return mChatId;
    }

    /**
     * Gets chat name.
     * @return chat name
     */
    public String getmChatName() {
        return mChatName;
    }

    /**
     * Gets chat members.
     * @return list of chat members
     */
    public List<Connection> getmChatMembers() {
        return mChatMembers;
    }

    /**
     * Gets chat message.
     * @return chat message
     */
    public ChatMessage getmRecentMsg() {
        return mRecentMsg;
    }

    @Override
    public String toString() {
        return "ChatRoomInfo{" +
                "mChatId=" + mChatId +
                ", mChatName='" + mChatName + '\'' +
                ", mChatMembers=" + mChatMembers +
                ", mRecentMsg=" + mRecentMsg +
                '}';
    }

    /**
     * Helper method that sorts each ChatRoomInfo in the order of most recent messages.
     *
     * @param list The list of ChatRoomInfo to be sorted
     * @return sorted list of ChatRoomInfo
     */
    public static List<ChatRoomInfo> sortList(List<ChatRoomInfo> list) {
        Collections.sort(list, new Comparator<ChatRoomInfo>() {
            @Override
            public int compare(ChatRoomInfo o1, ChatRoomInfo o2) {
                if (Objects.isNull(o1.mRecentMsg) && Objects.isNull(o2.mRecentMsg)) {
                    return 0;
                } else if (Objects.isNull(o1.mRecentMsg) && !Objects.isNull(o2.mRecentMsg)) {
                    return -1;
                } else if (!Objects.isNull(o1.mRecentMsg) && Objects.isNull(o2.mRecentMsg)) {
                    return 1;
                } else {
                    String s1 = o1.getmRecentMsg().getTimeStamp();
                    String s2 = o2.getmRecentMsg().getTimeStamp();
                    Timestamp t1 = Timestamp.valueOf(s1);
                    Timestamp t2 = Timestamp.valueOf(s2);
                    return -t1.compareTo(t2);
                }
            }
        });
        return list;
    }
}
