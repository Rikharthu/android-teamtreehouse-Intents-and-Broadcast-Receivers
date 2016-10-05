package com.teamtreehouse.musicmachine;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;


public class NetworkConnectionReceiver extends BroadcastReceiver {

    public static final String TAG = NetworkConnectionReceiver.class.getSimpleName();
    public static final String NOTIFY_NETWORK_CHANGE = "NOTIFY_NETWORK_CHANGE";
    public static final String EXTRA_IS_CONNECTED = "EXTRA_IS_CONNECTED";


    // FIXME DOESNT WORK
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,intent.getAction());

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());

        // check connection status
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected){
            showNotification(context,"("+currentDateandTime+") - Connected!");
        }else{
            showNotification(context,"("+currentDateandTime+") - Disconnected!");
        }

        Intent localIntent = new Intent(NOTIFY_NETWORK_CHANGE);
        localIntent.putExtra(EXTRA_IS_CONNECTED,isConnected);

        // broadcast to local process's receivers
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);

    }

    private void showNotification(Context context, String message){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_queue_music_white)
                        .setContentTitle("My notification")
                        .setContentText(message);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(33, mBuilder.build());
    }

}
