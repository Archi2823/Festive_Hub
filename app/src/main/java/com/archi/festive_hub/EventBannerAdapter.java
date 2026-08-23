package com.archi.festive_hub;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EventBannerAdapter extends RecyclerView.Adapter<EventBannerAdapter.BannerViewHolder> {

    private final String[] titles = {
            "Celebrate Together",
            "Festival of Colors",
            "Music & Food Fest"
    };

    private final String[] descriptions = {
            "Experience music, culture, food and unforgettable celebrations.",
            "Join the celebration of colors, joy and happiness.",
            "Enjoy live music, delicious food and amazing moments."
    };

    private final int[] colors = {
            Color.rgb(242, 107, 33),
            Color.rgb(255, 179, 0),
            Color.rgb(124, 77, 255)
    };

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_banner, parent, false);

        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull BannerViewHolder holder,
            int position
    ) {
        holder.title.setText(titles[position]);
        holder.description.setText(descriptions[position]);

        GradientDrawable background = new GradientDrawable();
        background.setColor(colors[position]);
        background.setCornerRadius(28f);

        holder.itemView.setBackground(background);
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.bannerTitle);
            description = itemView.findViewById(R.id.bannerDescription);
        }
    }
}