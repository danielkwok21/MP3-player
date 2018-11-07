package com.example.danie.mp3player;

import android.Manifest;
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
import android.widget.Button;
import android.widget.Toast;

import com.example.danie.mp3player.Adapters.MusicRecyclerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_BROWSE_STORAGE = 0;
    private static final int BROWSE_AUDIO = 0;
    RecyclerView musicRecyclerView;
    RecyclerView.LayoutManager layoutManager;
    List<File> musicList;
    MusicRecyclerAdapter musicRecyclerAdapter;

    MP3Player player;
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
        play = findViewById(R.id.main_play_btn);
        pause = findViewById(R.id.main_pause_btn);
        stop = findViewById(R.id.main_stop_btn);
        musicRecyclerView = findViewById(R.id.music_recyclerView);

        browse.setOnClickListener((v)->{
            browseDownloads();
        });

        //populating recyclerview
        musicList = getMusicFromStorage();
        layoutManager = new LinearLayoutManager(this);
        musicRecyclerView.setLayoutManager(layoutManager);
        musicRecyclerAdapter = new MusicRecyclerAdapter(musicList);
        musicRecyclerView.setHasFixedSize(true);
        musicRecyclerView.setAdapter(musicRecyclerAdapter);

    }

    private void getPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(this, "Explanation", Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_BROWSE_STORAGE);
            }
        }else{
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        }
    }

    private List<File> getMusicFromStorage(){
        final String SDCARD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
        File downloadFolder = new File(SDCARD);
        List<File> files = new ArrayList<>(Arrays.asList(downloadFolder.listFiles()));
        for(File file:files){
            Log.d(TAG, "File: "+file.getName());
        }

        return files;
    }

    //currently not working with emulator. Not sure why.
    //no music can be found in download folder
    private void browseDownloads(){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/mpeg");
        startActivityForResult(i, BROWSE_AUDIO);
        Toast.makeText(this, "browse", Toast.LENGTH_LONG).show();
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