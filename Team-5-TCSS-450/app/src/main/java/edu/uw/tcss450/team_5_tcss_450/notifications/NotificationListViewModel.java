package edu.uw.tcss450.team_5_tcss_450.notifications;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * View model for the list of Notifications.
 * Notifications are order from oldest first and newest last.
 *
 * @author Danieyll Wilson
 * @version June 6, 2021
 */
public class NotificationListViewModel extends AndroidViewModel {

    /**
     * MutableLiveData of a List of Notifications.
     */
    private MutableLiveData<List<Notification>> mNotificationList;

    /**
     * Public parameterized constructor.
     * Sets mNotificationList to be a MutableLiveData object
     * that will hold an Arraylist of Notification.
     *
     * @param application application
     */
    public NotificationListViewModel(@NonNull Application application) {
        super(application);
        mNotificationList = new MutableLiveData<>();
        mNotificationList.setValue(new ArrayList());
    }

    /**
     * Observes Notifications list.
     *
     * @param owner LifecycleOwner
     * @param observer List of Notifications to observe
     */
    public void addNotificationListObserver(@NonNull LifecycleOwner owner,
                                            @NonNull Observer<? super List<Notification>> observer) {
        mNotificationList.observe(owner, observer);
    }

    /**
     * Creates a new Notification and enters it into the Notification list.
     *
     * @param notification Notification to be inserted into the list.
     */
    public void handleNotification(final Notification notification) {
        int counter = 0;
        //Iterates through array to check certain notifications cases
        if (!mNotificationList.getValue().isEmpty()) {
            for (int i = 0; i < mNotificationList.getValue().size(); i++) {
                Notification temp = mNotificationList.getValue().get(i);
                /*If notification is the exact same type but different message
                cases:
                    User enters a chatroom with a recent chat notification on the homepage
                        for that certain chatroom already.
                    User receives a different new message from an user in a chatroom that
                        already has a new message notification on the homepage.
                    if a connection request notification gets duplicated, return from the function
                        (keep old notification and not enter the new notification duplication).
                 */
                if (temp.getType().toLowerCase().equals(notification.getType().toLowerCase())) {
                    if (temp.getType().toLowerCase().contains("connection request")) {
                        return;
                    } else {
                        mNotificationList.getValue().remove(i);
                        break;
                    }
                }
                //Recent chat notification cases, counts how many recent chat notifications inside list
                if (temp.getType().toLowerCase().contains("recent chat")) {
                    counter++;
                }
            }

            //If there are more than 3 recent chat notifications. finds the oldest recent chat
            //notification (first one to be found at the front of the list) and removes it from list.
            if (counter >= 3) {
                for (int j = 0; j < mNotificationList.getValue().size(); j++) {
                    Notification temp = mNotificationList.getValue().get(j);
                    if (temp.getType().toLowerCase().contains("recent chat")) {
                        mNotificationList.getValue().remove(j);
                        break;
                    }
                }
            }
        }

        //Enters new notification into the MutableLiveData<list<Notifications>>
        if (!mNotificationList.getValue().contains(notification)) {
            mNotificationList.getValue().add(notification);
        }
        mNotificationList.setValue(mNotificationList.getValue());
    }
    /**
     * Deletes all connections notifications from the list
     * */
    public void removeConnectionsNotifications(){
        if (!mNotificationList.getValue().isEmpty()) {
            for(int i = 0; i < mNotificationList.getValue().size(); i++){
                Notification notif = mNotificationList.getValue().get(i);
                if(notif.getType().toLowerCase().contains("connection request")){
                    mNotificationList.getValue().remove(i);
                }
            }
        }
        mNotificationList.setValue(mNotificationList.getValue());
    }

}

