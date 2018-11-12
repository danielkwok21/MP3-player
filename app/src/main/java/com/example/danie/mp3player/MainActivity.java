package com.example.danie.mp3player;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.danie.mp3player.Adapters.MusicRecyclerAdapter;
import com.example.danie.mp3player.Utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_BROWSE_STORAGE = 0;
    private static final String CHANNEL_ID = "MP3Player";
    private static final String NO_MUSIC = "No music selected";


    RecyclerView musicRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    List<String> musicList;
    MusicRecyclerAdapter musicRecyclerAdapter;
    static MP3Player player;
    static SeekBar progress;
    static Handler progressUpdateHandler;
    static Runnable progressUpdateRunnable;
    static Thread progressUpdateThread;
    static ImageView play;
    ImageView stop;
    SeekBar volume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();

        player = new MP3Player();

        musicRecyclerView = findViewById(R.id.music_recyclerView);
        progress = findViewById(R.id.main_progress_sb);
        play = findViewById(R.id.main_play_iv);
        stop = findViewById(R.id.main_stop_iv);
        volume = findViewById(R.id.main_volume_sb);

        setupRecyclerView();
        setupNotification(NO_MUSIC);

        play.setOnClickListener((v)->{
            togglePlayPause();
        });

        stop.setOnClickListener((v)->{
            stopMusic();
        });
    }

    private void setupSeekBar(){
        progress.setMax(player.getDuration());

        progressUpdateHandler = new Handler();
        progressUpdateRunnable = new Runnable(){
            @Override
            public void run() {
                progress.setProgress(player.getProgress());
                progressUpdateHandler.postDelayed(this, 0);
            }
        };
        progressUpdateThread = new Thread(progressUpdateRunnable);
        progressUpdateThread.start();

        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: progress: "+progress);
                if(fromUser){
                    player.setProgress(progress);
                    Log.d(TAG, "onProgressChanged: playerSet: "+progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch: ");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch: "+seekBar.getProgress());
                player.setProgress(seekBar.getProgress());
            }
        });
    }

    private void stopMusic(){
        player.stop();
        play.setImageResource(R.drawable.play);
    }

    private void setupRecyclerView(){

        //populating recyclerview
        musicList = getMusicFromStorage();
        layoutManager = new LinearLayoutManager(this);
        musicRecyclerView.setLayoutManager(layoutManager);
        musicRecyclerAdapter = new MusicRecyclerAdapter(musicList);
        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setAdapter(musicRecyclerAdapter);
    }

    private static void togglePlayPause(){
        switch(player.getState()){
            case PLAYING:
                player.pause();
                play.setImageResource(R.drawable.play);
                break;
            case PAUSED:
                player.play();
                progressUpdateHandler.removeCallbacks(progressUpdateRunnable);
                play.setImageResource(R.drawable.pause);
                break;
            case STOPPED:
                player.play();
                play.setImageResource(R.drawable.pause);
                break;
            default:
                player.play();
                play.setImageResource(R.drawable.play);
                break;
        }
    }

    public void selectMusic(Context c, View v){
        TextView tv = v.findViewById(R.id.main_music_name_tv);

        final String SDCARD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
        String musicName = tv.getText().toString();
        String musicPath = SDCARD+"/"+musicName;

        Util.Toast(c, musicName);

        if(player.getState()==MP3Player.MP3PlayerState.PLAYING){
            player.stop();
        }
        player.load(musicPath);
        setupSeekBar();
        setupNotification(musicName);
        play.setImageResource(R.drawable.pause);
    }

    private void setupNotification(String musicName){
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small_layout);
        notificationLayout.setTextViewText(R.id.notification_music_name_tv, musicName);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(false);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1, builder.build());
    }


    private void getPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Util.Toast(this, "Explanation");
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_BROWSE_STORAGE);
            }
        }else{
            Util.Toast(this, "Permission granted");
        }
    }

    private List<String> getMusicFromStorage(){
        final String SDCARD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
        final String[] audioFormats = {"mp3", "wav"};

        File downloadFolder = new File(SDCARD);
        File[] files = downloadFolder.listFiles();
        List<String> musicFiles = new ArrayList<>();

        for(File file:files){
            Log.d(TAG, "File: "+file.getName());
            String fileName = file.getName();
            String[] splitString = fileName.split("\\.");
            String format = splitString[splitString.length - 1];

            //if audioformat is one of the supported formats, name will be added to musicFiles
            for(String audioformat:audioFormats){
                if(format.equals(audioformat)){
                    musicFiles.add(file.getName());
                }
            }
        }

        return musicFiles;
    }

}