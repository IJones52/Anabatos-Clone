package edu.uw.tcss450.team_5_tcss_450.notifications;

/**
 * Notification template. Notification contains a String date, type, and message.
 * The user can build a notification with the builder class.
 *
 * @author Danieyll Wilson
 * @version Apr 5, 2021
 */
public class Notification {

    /**
     * Date variable of when the
     * notification was created.
     * Sample: Jan 1, 1970 12:00 AM.
     */
    private final String mDate;

    /**
     * Type variable of the notification.
     * Samples: new connection request, new conversation requests,
     * and new messages.
     */
    private final String mType;

    /**
     * Message variable that will contain
     * the message of the notification.
     * Sample: You have received a new conversation request...
     * You have received a new conversation request...
     * Message: ...
     */
    private final String mMessage;

    /**
     * Inner builder class to build a notification.
     */
    public static class Builder {

        /**
         * Date variable.
         */
        private final String mDate;

        /**
         * Type variable of the notification.
         */
        private String mType = "";

        /**
         * Message variable.
         */
        private String mMessage = "";

        /**
         * Parameterized constructor for builder class.
         * @param date date of the creation of notification.
         */
        public Builder(String date) {
            this.mDate = date;
        }

        /**
         * Adds type to the notification.
         * @param type Type of notification.
         * @return Type of notification.
         */
        public Builder addType(final String type) {
            mType = type;
            return this;
        }

        /**
         * Adds message to the notification.
         * @param Message Message of the notification
         * @return Message of the notification
         */
        public Builder addMessage(final String Message) {
            mMessage = Message;
            return this;
        }

        /**
         * Creates new Notification out of inner class builder's variables.
         * @return Notification with variables from the builder's variables.
         */
        public Notification build() {
            return new Notification(this);
        }
    }

    /**
     * Private parameterized constructor for Notification. Sets Notification variables
     * with builder variables.
     * @param builder builder variable to set Notification's variables.
     */
    private Notification(final Builder builder) {
        this.mDate = builder.mDate;
        this.mType = builder.mType;
        this.mMessage = builder.mMessage;
    }

    /**
     * Get date of specific Notification.
     * @return Date of creation of the Notification.
     */
    public String getDate() {
        return mDate;
    }

    /**
     * Get type of specific Notification.
     * @return Type of the Notification
     */
    public String getType() {
        return mType;
    }

    /**
     * Get message of specific Notification.
     * @return Message of the Notification.
     */
    public String getMessage() {
        return mMessage;
    }
}
