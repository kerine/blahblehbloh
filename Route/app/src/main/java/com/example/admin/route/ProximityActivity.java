package com.example.admin.route;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by admin on 30/10/15.
 */
public class ProximityActivity extends BroadcastReceiver {

    String notificationTitle;
    String notificationContent;
    String tickerMessage;
    Notification myNotification;
    NotificationManager notificationManager;

    ProximityActivity myReceiver;
    IntentFilter myIntentFiler;

    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {

        String key = LocationManager.KEY_PROXIMITY_ENTERING;

        Boolean proximity_entering = intent.getBooleanExtra(key, false);

        if (proximity_entering) {
            Log.d(getClass().getSimpleName(), "entering");
        } else {
            Log.d(getClass().getSimpleName(), "exiting");
        }

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context,FollowRouteActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setAutoCancel(false);
        builder.setTicker("Text");
        builder.setContentTitle("Title");
        builder.setContentText("You are near");
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setNumber(100);
        builder.build();

        myNotification = builder.getNotification();

        notificationManager.notify(11, myNotification);
    }

    public void onCreate() {
        myReceiver = new ProximityActivity();
        myIntentFiler = new IntentFilter("route.alert");
    }

    private Notification createNotification() {
        Notification notification = new Notification();
        notification.when = System.currentTimeMillis();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        notification.ledARGB = Color.WHITE;
        notification.ledOnMS = 1500;
        notification.ledOffMS = 1500;
        return notification;

    }


    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        boolean proximity_entering = getIntent().getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, true);

        Toast.makeText(this, "ProximityActivty = enter on create", Toast.LENGTH_SHORT).show();

        double lat = getIntent().getDoubleExtra("lat", 0);

        double lng = getIntent().getDoubleExtra("lng", 0);

        String strLocation = Double.toString(lat)+","+ Double.toString(lng);

        if(proximity_entering){
            Toast.makeText(getBaseContext(), "Entering the region", Toast.LENGTH_LONG).show();
            notificationTitle = "Proximity - Entry";
            notificationContent = "Entered the region:" + strLocation;
            tickerMessage = "Entered the region:" + strLocation;
        }else{
            Toast.makeText(getBaseContext(),"Exiting the region"  ,Toast.LENGTH_LONG).show();
            notificationTitle = "Proximity - Exit";
            notificationContent = "Exited the region:" + strLocation;
            tickerMessage = "Exited the region:" + strLocation;
        }

        Intent notificationIntent = new Intent(getApplicationContext(),NotificationActivity.class);

        /** Adding content to the notificationIntent, which will be displayed on
         * viewing the notification
         */
    /*
        notificationIntent.putExtra("content", notificationContent );

        /** This is needed to make this intent different from its previous intents
        notificationIntent.setData(Uri.parse("tel:/" + (int) System.currentTimeMillis()));

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        /** Creating different tasks for each notification. See the flag Intent.FLAG_ACTIVITY_NEW_TASK
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        /** Getting the System service NotificationManager
        NotificationManager nManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        /** Configuring notification builder to create a notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setWhen(System.currentTimeMillis())
                .setContentText(notificationContent)
                .setContentTitle(notificationTitle)
                .setAutoCancel(true)
                .setTicker(tickerMessage)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        /** Creating a notification from the notification builder
        Notification notification = notificationBuilder.build();

        /** Sending the notification to system.
         * The first argument ensures that each notification is having a unique id
         * If two notifications share same notification id, then the last notification replaces the first notification
         *
        nManager.notify((int)System.currentTimeMillis(), notification);

        /** Finishes the execution of this activity
        finish();
    }
*/
}
