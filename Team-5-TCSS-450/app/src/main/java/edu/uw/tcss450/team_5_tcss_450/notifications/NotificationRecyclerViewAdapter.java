package edu.uw.tcss450.team_5_tcss_450.notifications;

import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.databinding.FragmentNotificationCardBinding;
import edu.uw.tcss450.team_5_tcss_450.home.HomeFragmentDirections;

/**
 * An adapter for Notifications. Sets a list of Notifications into separate Notification cards.
 *
 * @author Danieyll Wilson
 * @version June 6, 2021
 */
public class NotificationRecyclerViewAdapter extends RecyclerView.Adapter<NotificationRecyclerViewAdapter.NotificationViewHolder> {

    /**
     * List variable to store all Notifications
     */
    private final List<Notification> mNotifications;

    /**
     * Parameterized constructor. Sets list of Notifications to local List variable
     * @param items List of Notifications
     */
    public NotificationRecyclerViewAdapter(List<Notification> items) {
        this.mNotifications = items;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NotificationViewHolder(LayoutInflater
        .from(parent.getContext())
        .inflate(R.layout.fragment_notification_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        holder.setNotification(mNotifications.get(position));
    }

    @Override
    public int getItemCount() {
        return mNotifications.size();
    }

    /**
     * Inner view holder class. Helps set Notification into a
     * Notification card.
     */
    public class NotificationViewHolder extends RecyclerView.ViewHolder {

        /**
         * View variable.
         */
        public final View mView;

        /**
         * Binding variable that contains the binding of the FragmentNotificationCard.
         */
        public FragmentNotificationCardBinding binding;

        /**
         * Notification variable.
         */
        private Notification mNotification;

        /**
         * Parameterize constuctor. Sets view and binding to according variables.
         * @param view
         */
        public NotificationViewHolder(View view) {
            super(view);
            mView = view;
            binding = FragmentNotificationCardBinding.bind(view);
        }

        /**
         * Sets Notification card with desired Notification.
         * @param notification Desired Notification info to be used on Notification Card
         */
        void setNotification(final Notification notification) {
            mNotification = notification;

            binding.textCardNotificationDate.setText(mNotification.getDate());
            if (mNotification.getMessage().length() >= 93) {
                String message = (new StringBuilder()).append(mNotification.getMessage().substring(0, 70)).append("...").toString();
                binding.textCardNotificationMessage.setText(message);
            } else {
                binding.textCardNotificationMessage.setText(mNotification.getMessage());
            }

            binding.textCardNotificationType.setText(mNotification.getType());

            if (notification.getType().toLowerCase().contains("new message")) {
                binding.imageCardNotificationImage.setImageIcon(Icon.createWithResource(mView.getContext(), R.drawable.ic_chat_unread_24dp));
                binding.layoutInner.setOnClickListener(button ->
                        Navigation.findNavController(mView).navigate(
                                HomeFragmentDirections
                                        .actionNavigationHomeToNavigationChat()
                        ));
            } else if (notification.getType().toLowerCase().contains("new connection")) {
                binding.imageCardNotificationImage.setImageIcon(Icon.createWithResource(mView.getContext(), R.drawable.ic_person_add_orange_24dp));
                binding.layoutInner.setOnClickListener(button ->
                        Navigation.findNavController(mView).navigate(
                                HomeFragmentDirections
                                        .actionNavigationHomeToConnectionRequestFragment()
                        ));
            } else if (notification.getType().toLowerCase().contains("new convers")) {
                binding.imageCardNotificationImage.setImageIcon(Icon.createWithResource(mView.getContext(), R.drawable.ic_chat_orange_24dp));
                binding.layoutInner.setOnClickListener(button ->
                        Navigation.findNavController(mView).navigate(
                                HomeFragmentDirections
                                        .actionNavigationHomeToNavigationChat()
                        ));
            } else if (notification.getType().toLowerCase().contains("recent chat")){
                binding.imageCardNotificationImage.setImageIcon(Icon.createWithResource(mView.getContext(), R.drawable.ic_chat_grey_24dp));
                binding.layoutInner.setOnClickListener(button ->
                        Navigation.findNavController(mView).navigate(
                                HomeFragmentDirections
                                        .actionNavigationHomeToNavigationChat()
                        ));
            }
            //Should never hit this error if we have all notifications down
            else {
                binding.imageCardNotificationImage.setBackgroundColor(Color.parseColor("#E11F1F"));
                binding.textCardNotificationType.setBackgroundColor(Color.parseColor("#E11F1F"));
                binding.textCardNotificationMessage.setBackgroundColor(Color.parseColor("#E11F1F"));
                binding.textCardNotificationDate.setBackgroundColor(Color.parseColor("#E11F1F"));
                Log.e("error in NotificationRecyclerViewAdapter at setNotification/", "notification.getType does not have a drawable image setup"); //TO-DO: add similar error to other binding views
            }
        }
    }
}
