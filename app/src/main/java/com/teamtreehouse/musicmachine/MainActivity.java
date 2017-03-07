package com.teamtreehouse.musicmachine;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.teamtreehouse.musicmachine.adapters.PlaylistAdapter;
import com.teamtreehouse.musicmachine.models.Song;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_FAVORITE = "EXTRA_FAVORITE";
    public static final String EXTRA_TITLE="EXTRA_TITLE";
    public static final String EXTRA_SONG = "EXTRA_SONG";
    public static final int REQUEST_FAVORITE = 0;
    public static final String EXTRA_LIST_POSITION = "EXTRA_LIST_POSITION";

    private boolean mBound = false;
    private Button mDownloadButton;
    private Button mPlayButton;
    private Messenger mServiceMessenger;
    private Messenger mActivityMessenger = new Messenger(new ActivityHandler(this));
    private RelativeLayout mRootLayout;

    private PlaylistAdapter mAdapter;

    private NetworkConnectionReceiver mReceiver=new NetworkConnectionReceiver();

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(LOG_TAG,"onServiceConnected() - "+name);
            mBound = true;
            mServiceMessenger = new Messenger(binder);
            Message message = Message.obtain();
            message.arg1 = 2;
            message.arg2 = 1;
            message.replyTo = mActivityMessenger;
            try {
                mServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDownloadButton = (Button) findViewById(R.id.downloadButton);
        mPlayButton = (Button) findViewById(R.id.playButton);

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadSongs();
//                testIntents();
            }
        });
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound) {
                    Intent intent = new Intent(MainActivity.this, PlayerService.class);
                    intent.putExtra(EXTRA_SONG, Playlist.songs[0]);
                    startService(intent);
                    Message message = Message.obtain();
                    message.arg1 = 2;
                    message.replyTo = mActivityMessenger;
                    try {
                        mServiceMessenger.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        mAdapter = new PlaylistAdapter(this, Playlist.songs);
        recyclerView.setAdapter(mAdapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        mRootLayout= (RelativeLayout) findViewById(R.id.rootLayout);

    }

    private void testIntents() {
        // Explicit intent
//        Intent intent = new Intent(this,DetailActivity.class);
//        intent.putExtra(EXTRA_TITLE,"Gradle, Gradle, Gradle");
//        //startActivity(intent);
//        startActivityForResult(intent,REQUEST_FAVORITE);


        // Implicit intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
//        Uri geoLocation = Uri.parse("geo:56.9496,24.1052");
        Uri geoLocation = Uri.parse("geo:0,0?q=56.971096, 24.160521(Accenture)");
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }else{
            // handle error
            Snackbar.make(mRootLayout, "Sorry, nothing found to handle this request",
                    Snackbar.LENGTH_LONG).show();
        }

        /*
        // Take a user to Google Play to download an app that can handle a specific kind of Intent
        Uri googlePlayUri = Uri.parse("market://search?q=map");
        Intent googlePlayIntent = new Intent();
        googlePlayIntent.putExtra(Intent.ACTION_VIEW, googlePlayUri);

        if (googlePlayIntent.resolveActivity(getPackageManager()) == null) {
            Snackbar.make(mRootLayout, "Sorry, nothing found to handle this request.", Snackbar.LENGTH_LONG).show();
        }
        else {
            startActivity(googlePlayIntent);
        }
        */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case REQUEST_FAVORITE:
                // favorite song
                if(resultCode==RESULT_OK){
                    boolean result = data.getBooleanExtra(EXTRA_FAVORITE,false);
                    int position = data.getIntExtra(EXTRA_LIST_POSITION,0);
                    // update model
                    Playlist.songs[position].setIsFavorite(result);
                    // notify adapter
                    mAdapter.notifyItemChanged(position);
                    Log.d(LOG_TAG,"is favorite? "+result);
                }else if(resultCode==RESULT_CANCELED){
                    // back button pressed
                    Log.d(LOG_TAG,"canceled");
                }
                break;
        }

    }

    private void downloadSongs() {
        Toast.makeText(MainActivity.this, "Downloading", Toast.LENGTH_SHORT).show();

        // Send Messages to Handler for processing
        for (Song song : Playlist.songs) {
            Intent intent = new Intent(MainActivity.this, DownloadIntentService.class);
            intent.putExtra(EXTRA_SONG, song);
            startService(intent);
        }
    }

    public void changePlayButtonText(String text) {
        mPlayButton.setText(text);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }

    // Connect and disconnect BroadcastReceiver in lifecycle methods
    // That way Broadcasts will be received only when app is running
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mReceiver,filter);

        // register another custom receiver
        IntentFilter customFilter = new IntentFilter(NetworkConnectionReceiver.NOTIFY_NETWORK_CHANGE);
        // register to local process broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalReceiver,customFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        // unregister from local receivers
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalReceiver);
    }


    private BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG,"onReceive()");
            boolean isConnected = intent.getBooleanExtra(NetworkConnectionReceiver.EXTRA_IS_CONNECTED,false);
            if(isConnected){
                Snackbar.make(mRootLayout,"Network is connected.",Snackbar.LENGTH_LONG).show();
            }else{
                Snackbar.make(mRootLayout,"Network is disconnected.",Snackbar.LENGTH_LONG).show();
            }
        }
    };

}
