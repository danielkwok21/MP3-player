package com.example.danie.mp3player.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.danie.mp3player.R;

import java.io.File;
import java.util.List;

public class MusicRecyclerAdapter extends RecyclerView.Adapter<MusicRecyclerAdapter.MusicViewHolder> {

    private List<File> musicList;

    public MusicRecyclerAdapter(List<File> list){
        musicList = list;
    }


    //primary method #1
    //to inflate item layout and create holder
    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        TextView tv = (TextView)layoutInflater.inflate(R.layout.music_view_layout, viewGroup, false);
        MusicViewHolder musicViewHolder = new MusicViewHolder(tv);

        return musicViewHolder;
    }

    //primary method #2
    //to set view attr based on data
    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder musicViewHolder, int i) {
        musicViewHolder.musicName.setText(musicList.get(i).getName());
    }

    //primary method #3
    //to determine number of items
    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder{
        TextView musicName;

        public MusicViewHolder(@NonNull TextView itemView) {
            super(itemView);
            musicName = itemView;
        }
    }
}
