package com.example.danie.mp3player;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
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
import com.example.danie.mp3player.Services.MusicPlayerService;
import com.example.danie.mp3player.Services.MusicPlayerService.MusicPlayerBinder;
import com.example.danie.mp3player.Utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_BROWSE_STORAGE = 0;
    private static final String CHANNEL_ID = "MP3Player";
    private static final String NO_MUSIC = "No music selected";
    private static final int NOTI_ID = 1;
    private static String currentName = NO_MUSIC;
    private static int currentProgress = 0;
    private static final String SDCARD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();


    private RecyclerView musicRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<String> musicList;
    private MusicRecyclerAdapter musicRecyclerAdapter;
    private MusicPlayerService player;
    private Handler progressUpdateHandler;
    private Runnable progressUpdateRunnable;
    private Thread progressUpdateThread;
    private Handler notificationHandler;
    private Runnable notificationRunnable;
    private Thread notificationThread;
    private Handler recyclerViewHandler;
    private Runnable recyclerViewRunnable;
    private Thread recyclerViewThread;
    private NotificationManagerCompat notificationManagerCompat;

    //UI components
    private SeekBar progress;
    private ImageView shuffle;
    private ImageView prev;
    private ImageView play;
    private ImageView next;
    private ImageView repeat;
    private ImageView volumeIcon;
    private SeekBar volume;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get user permission during runtime
        getPermission();

        //start MusicPlayerService
        Intent i = new Intent(this, MusicPlayerService.class);
        startService(i);
        bindService(i, connection, Context.BIND_AUTO_CREATE);

        initComponents();

        setupRecyclerView();

        prev.setOnClickListener((v)->{
            prev();
        });

        play.setOnClickListener((v)->{
            playPause();
        });

        next.setOnClickListener((v)->{
            next();
        });
    }

    private void initComponents(){
        musicRecyclerView = findViewById(R.id.music_recyclerView);
        progress = findViewById(R.id.main_progress_sb);
        shuffle = findViewById(R.id.main_shuffle_iv);
        prev = findViewById(R.id.main_prev_iv);
        play = findViewById(R.id.main_play_iv);
        next = findViewById(R.id.main_next_iv);
        repeat = findViewById(R.id.main_repeat_iv);
        volumeIcon = findViewById(R.id.main_volume_iv);
        volume = findViewById(R.id.main_volume_sb);
    }

    //setting up service connection
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerBinder binder = (MusicPlayerBinder) service;
            player = binder.getService();
            setupProgressBar();
            switch (player.getState()){
                case PLAYING:
                    play.setImageResource(R.drawable.pause);
                    break;
                case PAUSED:
                    play.setImageResource(R.drawable.play);
                    break;
                case STOPPED:
                    play.setImageResource(R.drawable.play);
                    break;
                default:
                    play.setImageResource(R.drawable.play);
                    break;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void setupRecyclerView(){

        recyclerViewHandler = new Handler();
        recyclerViewRunnable = new Runnable(){
            @Override
            public void run() {
                //populating recyclerview
                musicList = getMusicFromStorage();
                layoutManager = new LinearLayoutManager(getApplicationContext());
                musicRecyclerView.setLayoutManager(layoutManager);
                musicRecyclerAdapter = new MusicRecyclerAdapter(musicList);
                musicRecyclerView.setHasFixedSize(true);
                musicRecyclerView.setAdapter(musicRecyclerAdapter);

            }
        };
        recyclerViewThread = new Thread(recyclerViewRunnable);
        recyclerViewThread.start();
    }

    //setup music progress bar
    private void setupProgressBar(){
        progress.setMax(player.getSongDuration());

        try{
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
                    if(fromUser){
                        player.setProgress(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    player.setProgress(seekBar.getProgress());
                }
            });
        }catch(Exception e){
            Log.d(TAG, "setupProgressBar: "+e);
        }
    }

    private void prev(){
        player.stop();
        play.setImageResource(R.drawable.play);
    }

    private void next(){
        player.next();
        loadNextSong();
    }

    private void loadNextSong(){
        int index = musicList.indexOf(currentName);
        //get next song. If song is at end of list, loop to top
        if(index<musicList.size()-1){
            currentName = musicList.get(index+1);
        }else{
            currentName = musicList.get(0);
        }
        setMusic();
    }

    private void playPause(){
        switch(player.getState()){
            case PLAYING:
                player.pause();
                play.setImageResource(R.drawable.play);
                break;
            case PAUSED:
                player.play();
                play.setImageResource(R.drawable.pause);
                progressUpdateHandler.removeCallbacks(progressUpdateRunnable);
                break;
            case STOPPED:
                if(setMusic()){
                    play.setImageResource(R.drawable.pause);
                }else{
                    Util.Toast(getApplicationContext(), "No music selected");
                }
                break;
            default:
                play.setImageResource(R.drawable.play);
                player.play();
                break;
        }
    }

    private List<String> getMusicFromStorage(){
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

    /*
    * loads music based on currentMusicName
    * change play/pause button view
    * setup notification
    * */
    private boolean setMusic(){
        if(player.getState()==MP3Player.MP3PlayerState.PLAYING){
            player.stop();
        }

        if(!currentName.equals(NO_MUSIC)){
            String musicPath = SDCARD+"/"+currentName;
            player.load(musicPath);

            setupProgressBar();
            setupNotification();
            play.setImageResource(R.drawable.pause);

            return true;
        }
        return false;
    }

    /*
    * if music is selected from recycler view
    * */
    public void selectMusicFromView(View v){
        TextView tv = v.findViewById(R.id.main_music_name_tv);

        String musicName = tv.getText().toString();
        currentName = musicName;
        setMusic();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentName", currentName);
        outState.putInt("currentProgress", player.getProgress());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentName = savedInstanceState.getString("currentName");
        currentProgress = savedInstanceState.getInt("currentProgress");
    }

    private void setupNotification(){
        notificationHandler = new Handler();

        //does not start a new task if current app is running
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        notificationRunnable = new Runnable(){
            @Override
            public void run() {

                RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_small_layout);
                notificationLayout.setTextViewText(R.id.notification_music_name_tv, currentName);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setCustomContentView(notificationLayout)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pi)
                        .setAutoCancel(false);

                notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                notificationManagerCompat.notify(NOTI_ID, builder.build());
            }
        };
        notificationThread = new Thread(notificationRunnable);
        notificationThread.start();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(connection!=null){
            unbindService(connection);
            connection = null;
        }
    }
}