package com.example.danie.mp3player.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.example.danie.mp3player.MP3Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerService extends Service {
    private static final String TAG = "MusicPlayerService";
    private IBinder musicPlayerbinder;
    private MP3Player player;
    private String musicName = "No music";

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(player!=null){
            player = null;
        }
        player = new MP3Player();
        musicPlayerbinder = new MusicPlayerBinder();

    }

    public String getName(){
        return musicName;
    }

    public int getProgress(){
        return player.getProgress();
    }

    public void setProgress(int progress){
        player.setProgress(progress);
    }

    public int getMusicDuration(){
        return player.getDuration();
    }

    public MP3Player.MP3PlayerState getState(){
        return player.getState();
    }

    public void stop(){
        player.stop();
    }

    public void pause(){
        player.pause();
    }
    public void play(){
        player.play();
    }

    public void load(String musicPath){
        player.load(musicPath);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicPlayerbinder;
    }

    public class MusicPlayerBinder extends Binder{
        public MusicPlayerService getService(){
            return MusicPlayerService.this;
        }
    }
}
