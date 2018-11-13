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

    //gets current song progress in milliseconds
    public int getProgress(){
        return player.getProgress();
    }

    //gets current loaded song name
    public String getCurrentSongName(){
        String songName = null;
        String filePath = player.getFilePath();
        if(filePath!=null){
            String[] splitString = filePath.split("/");
            songName = splitString[splitString.length - 1];
            Log.d(TAG, "getCurrentSongName: SongName: "+songName);
        }
        return songName;
    }

    //sets progress of current loaded song
    public void setProgress(int progress){
        player.setProgress(progress);
    }

    //gets duration of current loaded song
    public int getSongDuration(){
        return player.getDuration();
    }

    //gets current state of mediaplayer (play, paused, stopped, etc)
    public MP3Player.MP3PlayerState getState(){
        return player.getState();
    }

    //stops music
    public void stop(){
        player.stop();
    }

    //pauses music
    public void pause(){
        player.pause();
    }

    //plays music
    public void play(){
        player.play();
    }

    //gets progress in timeformat of current loaded song
    public String getProgressInTime(){
        //transfer the millisecond to minutes
            SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
            return sdf.format(player.getProgress());
    }

    //gets completion status of current loaded song
    public boolean getCompletionStatus(){
        if(player.getState()==MP3Player.MP3PlayerState.PLAYING){
            return player.getProgress()>=player.getDuration();
        }
        return false;
    }

    //loads up song from path given
    public void load(String songPath){
        player.load(songPath);
    }

    //service binder
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
