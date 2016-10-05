package com.teamtreehouse.musicmachine;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.teamtreehouse.musicmachine.models.Song;

public class DownloadIntentService extends IntentService {
    private static final String TAG = DownloadIntentService.class.getSimpleName();
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID=1488;

    public DownloadIntentService() {
        super("DownloadIntentService");
        setIntentRedelivery(true);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Song song = intent.getParcelableExtra(MainActivity.EXTRA_SONG);
        String title = song.getTitle();

        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_queue_music_white)
                .setContentTitle("Downoading")
                .setContentText(title);
        mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(NOTIFICATION_ID,builder.build());

        downloadSong(title);
    }

    private void downloadSong(String song) {
        long endTime = System.currentTimeMillis() + 2*1000;
        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // cancel notification
        mNotificationManager.cancel(NOTIFICATION_ID);

        Log.d(TAG, song + " downloaded!");
    }
}
