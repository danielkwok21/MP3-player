package com.example.danie.mp3player.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.danie.mp3player.MP3Player;

import java.text.SimpleDateFormat;

public class MusicPlayerService extends Service {
    private static final String TAG = "MusicPlayerService";
    private IBinder musicPlayerBinder;
    private MP3Player player;
    private String songName = "None";

    public MusicPlayerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(player!=null){
            player = null;
        }
        player = new MP3Player();
        musicPlayerBinder = new MusicPlayerBinder();

    }

    public int getProgress(){
        return player.getProgress();
    }

    public void setProgress(int progress){
        player.setProgress(progress);
    }

    public int getSongDuration(){
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

    public String getProgressInTime(){
        //transfer the millisecond to minutes
            SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
            return sdf.format(player.getProgress());
    }

    public String getProgressInTime(int progress){
        //transfer the millisecond to minutes
        SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
        return sdf.format(progress);
    }

    public boolean getCompletionStatus(){
        if(player.getState()==MP3Player.MP3PlayerState.PLAYING){
            return player.getProgress()>=player.getDuration();
        }
        return false;
    }
    
    public void next(){
        int duration = player.getDuration();
        player.setProgress(duration);
    }


    public void load(String songPath){
        player.load(songPath);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicPlayerBinder;
    }

    public class MusicPlayerBinder extends Binder{
        public MusicPlayerService getService(){
            return MusicPlayerService.this;
        }
    }
}
