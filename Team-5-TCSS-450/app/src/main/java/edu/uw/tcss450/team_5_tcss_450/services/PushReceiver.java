package edu.uw.tcss450.team_5_tcss_450.services;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;

import edu.uw.tcss450.team_5_tcss_450.AuthActivity;
import edu.uw.tcss450.team_5_tcss_450.R;
import edu.uw.tcss450.team_5_tcss_450.ui.chat.ChatMessage;

import edu.uw.tcss450.team_5_tcss_450.ui.connections.Connection;
import me.pushy.sdk.Pushy;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;

public class PushReceiver extends BroadcastReceiver {
    public static final String RECEIVED_NEW_MESSAGE = "new message from Pushy";
    public static final String RECEIVED_CONNECTION_UPDATE = "Connections Updated";
    private static final String CHANNEL_ID = "1";

    @Override
    public void onReceive(Context context, Intent intent) {
        String typeOfMessage = intent.getStringExtra("type");

        // init message + chatId values
        ChatMessage message = null;
        int chatId = -1;

        Connection connection = null;

        // try parsing the json to a ChatMessage object
        if(intent.getStringExtra("type").equals("msg")){
            try {
                message = ChatMessage.createFromJsonString(intent.getStringExtra("message"));
                chatId = intent.getIntExtra("chatid", -1);
                Log.d("PUSHY", "parsing message: " + message);
            } catch (JSONException e) {
                // Web service sent us something unexpected.
                Log.e("PushReceiver", "onReceive: " + e.getMessage());
                throw new IllegalStateException("Error from Web Service. Contact Dev Support.");
            }

            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);

            if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE) {
                Log.d("PUSHY", "Message received in foreground: " + message.getMessage());

                // create an Intent to broadcast a message to other parts of the app.
                Intent i = new Intent(RECEIVED_NEW_MESSAGE);
                i.putExtra("chatMessage", message);
                i.putExtra("chatId", chatId);
                i.putExtras(intent.getExtras());

                context.sendBroadcast(i);
            } else {
                // app is in background so create and post a notification
                Log.d("PUSHY", "Message received in background: " + message.getMessage());

                Intent i = new Intent(context, AuthActivity.class);
                i.putExtras(intent.getExtras());

                PendingIntent pendingIntent = PendingIntent
                        .getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_chat_black_24dp)
                        .setContentTitle("Message from: " + message.getSender())
                        .setContentText(message.getMessage())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent);

                // Automatically configure a ChatMessageNotification Channel
                Pushy.setNotificationChannel(builder, context);

                // Get an instance of the NotificationManager service
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(context.NOTIFICATION_SERVICE);

                // Build the notification and display it
                notificationManager.notify(1, builder.build());
            }

        }
        /**
         * I tried to emulate the notifications things to chat but i'm not sure if they're working
         * All the info you need to make a notification for contacts stuff is here
         * */
        if (typeOfMessage.equals("request")) {
            Log.d("Request", "Received");
            try {
                connection = Connection.createFromJsonString(intent.getStringExtra("sender"));

            } catch (JSONException e) {
                // Web service sent us something unexpected.
                Log.e("PushReceiver", "onReceive: " + e.getMessage());
                throw new IllegalStateException("Error from Web Service. Contact Dev Support.");
            }


            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);

            // app is in foreground so send the message to the active Activities
            if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                Log.d("PUSHY", "Request received in foreground: " + connection);

                // create an Intent to broadcast a message to other parts of the app.
                Intent i = new Intent(RECEIVED_CONNECTION_UPDATE);
                i.putExtra("request", connection);
                i.putExtras(intent.getExtras());

                context.sendBroadcast(i);
            } else { // app is in background so create and post a notification
                Log.d("PUSHY", "Request received in background: " + connection.getName());

                Intent i = new Intent(context, AuthActivity.class);
                i.putExtras(intent.getExtras());

                PendingIntent pendingIntent = PendingIntent
                        .getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_person_add_orange_24dp)
                        .setContentTitle(connection.getName() + " sent a request!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent);

                // Automatically configure a ChatMessageNotification Channel
                Pushy.setNotificationChannel(builder, context);

                // Get an instance of the NotificationManager service
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(context.NOTIFICATION_SERVICE);

                // Build the notification and display it
                notificationManager.notify(1, builder.build());
            }

        }
        if (typeOfMessage.equals("connection")) {
            Log.d("Connection", "Accepted");
            try {
                connection = Connection.createFromJsonString(intent.getStringExtra("receiver"));

            } catch (JSONException e) {
                // Web service sent us something unexpected.
                Log.e("PushReceiver", "onReceive: " + e.getMessage());
                throw new IllegalStateException("Error from Web Service. Contact Dev Support.");
            }


            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);

            // app is in foreground so send the message to the active Activities
            if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                Log.d("PUSHY", "Connection accepted in foreground: " + connection);

                // create an Intent to broadcast a message to other parts of the app.
                Intent i = new Intent(RECEIVED_CONNECTION_UPDATE);
                i.putExtra("connections", connection);
                i.putExtras(intent.getExtras());


                context.sendBroadcast(i);
            } else { // app is in background so create and post a notification
                Log.d("PUSHY", "Connection accepted in background: " + connection.getName());





                Intent i = new Intent(context, AuthActivity.class);
                i.putExtras(intent.getExtras());

                PendingIntent pendingIntent = PendingIntent
                        .getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                context.sendBroadcast(i);



                // Get an instance of the NotificationManager service
                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_chat_black_24dp)
                        .setContentTitle("Message from: " + message.getSender())
                        .setContentText(message.getMessage())
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent);


                // Build the notification and display it
                notificationManager.notify(1, builder.build());
            }


        }

        if(typeOfMessage.equals("update")){

            String list = intent.getStringExtra("list");


            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);

            // app is in foreground so send the message to the active Activities
            if (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                Log.d("PUSHY", "List updated " + list);

                // create an Intent to broadcast a message to other parts of the app.
                Intent i = new Intent(RECEIVED_CONNECTION_UPDATE);
                i.putExtra("update", list);
                i.putExtras(intent.getExtras());
                context.sendBroadcast(i);
            }
        }

    }
}