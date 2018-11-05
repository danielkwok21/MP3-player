package com.example.danie.mp3player;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int BROWSE_AUDIO = 0;

    MP3Player player;
    Button browse;
    Button play;
    Button pause;
    Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        player = new MP3Player();
        player.load("/sdcard/Download/sample.mp3");

        browse = findViewById(R.id.main_browse_btn);
        play = findViewById(R.id.main_play_btn);
        pause = findViewById(R.id.main_pause_btn);
        stop = findViewById(R.id.main_stop_btn);

        browse.setOnClickListener((v)->{
            browseDownloads();
        });
    }

    private void browseDownloads(){
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.setType("audio/mpeg");
//        startActivityForResult(i, BROWSE_AUDIO);
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