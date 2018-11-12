package com.example.danie.mp3player.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.danie.mp3player.MainActivity;
import com.example.danie.mp3player.R;
import java.util.List;

public class MusicRecyclerAdapter extends RecyclerView.Adapter<MusicRecyclerAdapter.MusicViewHolder> {

    private List<String> musicList;

    public MusicRecyclerAdapter(List<String> list){
        musicList = list;
    }


    //primary method #1
    //to inflate item layout and create holder
    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View v = layoutInflater.inflate(R.layout.music_view_layout, viewGroup, false);
        MusicViewHolder musicViewHolder = new MusicViewHolder(v, context);

        return musicViewHolder;
    }

    //primary method #2
    //to set view attr based on data
    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder musicViewHolder, int i) {
        musicViewHolder.musicName.setText(musicList.get(i));
    }

    //primary method #3
    //to determine number of items
    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView musicName;
        private Context context;

        public MusicViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            musicName = itemView.findViewById(R.id.main_music_name_tv);
            this.context = context;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(context instanceof MainActivity){
                ((MainActivity)context).selectMusicFromView(context, v);
            }
        }
    }

}
