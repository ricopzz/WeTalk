package com.example.enrico.myapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by enrico on 29/12/17.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle(); // retrieve from nodejs
        String notification_message = remoteMessage.getNotification().getBody();

        String clickAction = remoteMessage.getNotification().getClickAction(); // get the click action of notifications

        String from_user_id = remoteMessage.getData().get("from_user_id"); // retrieve the data from user id to get the extra

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(notification_title)
                        .setContentText(notification_message);

        //intent to go to layout that the notification is showing
        Intent resultIntent = new Intent(clickAction);
        //extra to get the userid because profileactivity needs to pass id for usersactivity
        resultIntent.putExtra("user_id",from_user_id);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, mBuilder.build());

    }
}
