package com.example.mpproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mpproject.RankingItem;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<RankingItem> rankingList;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView playerNameTextView;
        public TextView scoreTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            playerNameTextView = itemView.findViewById(R.id.playerNameTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
        }
    }

    public RankingAdapter(List<RankingItem> rankingList) {
        this.rankingList = rankingList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ranking_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RankingItem currentItem = rankingList.get(position);

        holder.playerNameTextView.setText(currentItem.getPlayerName());
        holder.scoreTextView.setText(String.valueOf(currentItem.getScore()));
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }
}