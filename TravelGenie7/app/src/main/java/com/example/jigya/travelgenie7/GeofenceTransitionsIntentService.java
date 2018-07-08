package com.example.jigya.travelgenie7;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    private static final String TAG=GeofenceTransitionsIntentService.class.getSimpleName();
    public static final int GEOFENCE_NOTIFICATION_ID=0;

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {

        GeofencingEvent geofencingEvent=GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError())
        {
            String errorMsg=getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG,errorMsg) ;
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails);
            Log.i(TAG, geofenceTransitionDetails);
        }



    }

    private String getGeofenceTransitionDetails( int geofenceTransition, List<Geofence> triggeringGeofences)
    {
        ArrayList<String> triggeringGeofencesList=new ArrayList<>();
        for (Geofence geofence :triggeringGeofences)
        {
            triggeringGeofencesList.add(geofence.getRequestId());
        }
        String status=null;
        if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER)
        {
            status="Entering";
        }
        else if(geofenceTransition==Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            status="Exiting";
        }
        return status+ TextUtils.join(", ",triggeringGeofencesList);
    }

    private void sendNotification(String msg) {
        Log.i(TAG,"Send Notification: "+msg);
        Intent notificationIntent=CreateGeofence.makeNotificationIntent(getApplicationContext(),msg);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(this);
        stackBuilder.addParentStack(CreateGeofence.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent=stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(GEOFENCE_NOTIFICATION_ID ,createNotification(msg,notificationPendingIntent));



    }
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent)

    {
        NotificationCompat.Builder notificationBuilder=new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE |Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();

    }

    private static String getErrorString(int errorCode)
    {
        switch (errorCode)
        {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "Geofence Not Available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too Many Geofences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too Many Pending Intents";
            default:
                return "Unknown Error";
        }
    }


}
