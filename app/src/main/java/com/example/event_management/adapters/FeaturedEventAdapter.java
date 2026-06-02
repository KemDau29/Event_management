package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.event_management.R;
import com.example.event_management.models.Event;
import java.util.ArrayList;
import java.util.List;

public class FeaturedEventAdapter extends RecyclerView.Adapter<FeaturedEventAdapter.FeaturedViewHolder> {

    private final Context context;
    private List<Event> featuredList = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public FeaturedEventAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFeaturedList(List<Event> list) {
        this.featuredList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FeaturedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_featured_event, parent, false);
        return new FeaturedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedViewHolder holder, int position) {
        Event event = featuredList.get(position);
        if (event != null) {
            holder.tvTitle.setText(event.getTitle());
            holder.tvDate.setText(String.format("📅  %s", event.getFormattedDate()));
            holder.tvLocation.setText(String.format("📍  %s", event.getLocation()));
            holder.tvAttendants.setText(String.format("🔥 %d lượt đăng ký", event.getAttendants()));

            if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
                Glide.with(context)
                        .load(event.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.imgFeatured);
            } else {
                holder.imgFeatured.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(event);
            });
        }
    }

    @Override
    public int getItemCount() {
        return featuredList.size();
    }

    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFeatured;
        TextView tvTitle, tvDate, tvLocation, tvAttendants;

        public FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFeatured = itemView.findViewById(R.id.imgFeatured);
            tvTitle = itemView.findViewById(R.id.tvFeaturedTitle);
            tvDate = itemView.findViewById(R.id.tvFeaturedDate);
            tvLocation = itemView.findViewById(R.id.tvFeaturedLocation);
            tvAttendants = itemView.findViewById(R.id.tvFeaturedAttendants);
        }
    }
}