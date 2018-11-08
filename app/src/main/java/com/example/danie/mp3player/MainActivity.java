package com.example.danie.mp3player;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.mp3player.Adapters.MusicRecyclerAdapter;
import com.example.danie.mp3player.Utils.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_BROWSE_STORAGE = 0;
    private static final int BROWSE_AUDIO = 0;

    RecyclerView musicRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    List<String> musicList;
    MusicRecyclerAdapter musicRecyclerAdapter;
    static MP3Player player;
    Button browse;
    Button play;
    Button pause;
    Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();

        browse = findViewById(R.id.main_browse_btn);
        player = new MP3Player();
        play = findViewById(R.id.main_play_btn);
        pause = findViewById(R.id.main_pause_btn);
        stop = findViewById(R.id.main_stop_btn);
        musicRecyclerView = findViewById(R.id.music_recyclerView);

        browse.setOnClickListener((v)->{
            browseDownloads();
        });

        play.setOnClickListener((v)->{
            player.play();
        });

        pause.setOnClickListener((v)->{
            player.pause();
        });

        stop.setOnClickListener((v)->{
            player.stop();
        });

        //populating recyclerview
        musicList = getMusicFromStorage();
        layoutManager = new LinearLayoutManager(this);
        musicRecyclerView.setLayoutManager(layoutManager);
        musicRecyclerAdapter = new MusicRecyclerAdapter(musicList);
        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setAdapter(musicRecyclerAdapter);

    }

    public static void setMusic(Context c, View v){
        TextView tv = v.findViewById(R.id.main_music_name_tv);

        final String SDCARD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
        String musicName = tv.getText().toString();
        String musicPath = SDCARD+"/"+musicName;

        Util.Toast(c, musicName);

        if(player.getState()==MP3Player.MP3PlayerState.PLAYING){
            player.stop();
        }
        player.load(musicPath);
        player.play();

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

    //currently not working with emulator. Not sure why.
    //no music can be found in download folder
    private void browseDownloads(){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/mpeg");
        startActivityForResult(i, BROWSE_AUDIO);
        Util.Toast(this, "browse");
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);
        switch(resultCode){
            case 0:
                player.load(data.getDataString());
                break;
            default:
                Log.e(TAG, "Couldn't load data");
        }
    }
}