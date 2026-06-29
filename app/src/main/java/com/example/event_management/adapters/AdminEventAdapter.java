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
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.AdminEventViewHolder> {
    private Context context;
    private List<Event> eventList = new ArrayList<>();
    private OnEventActionListener listener;

    public interface OnEventActionListener {
        void onEdit(Event event);
        void onDelete(Event event);
        void onDetail(Event event);
    }

    public AdminEventAdapter(Context context, OnEventActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setEventList(List<Event> list) {
        this.eventList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_event, parent, false);
        return new AdminEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventViewHolder holder, int position) {
        Event event = eventList.get(position);
        if (event != null) {
            holder.tvName.setText(event.getTitle());
            holder.tvLocation.setText(event.getLocation());
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%,dđ", event.getPrice()));
            
            if (event.isLimited()) {
                holder.tvRemaining.setVisibility(View.VISIBLE);
                if (event.getRemainingTickets() <= 0) {
                    holder.tvRemaining.setText("HẾT VÉ");
                    holder.tvRemaining.setTextColor(android.graphics.Color.RED);
                } else {
                    holder.tvRemaining.setText("Vé còn: " + event.getRemainingTickets());
                    holder.tvRemaining.setTextColor(android.graphics.Color.parseColor("#4CAF50")); // Green
                }
            } else {
                holder.tvRemaining.setVisibility(View.GONE);
            }

            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(context).load(event.getImageUrl()).into(holder.img);
            } else {
                holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(event));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(event));
            holder.cardForeground.setOnClickListener(v -> listener.onDetail(event));
        }
    }

    @Override
    public int getItemCount() { return eventList.size(); }

    public static class AdminEventViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public MaterialButton btnEdit, btnDelete;
        public TextView tvName, tvLocation, tvPrice, tvRemaining;
        public View cardForeground;

        public AdminEventViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgAdminEvent);
            tvName = itemView.findViewById(R.id.tvAdminEventName);
            tvLocation = itemView.findViewById(R.id.tvAdminEventLocation);
            tvPrice = itemView.findViewById(R.id.tvAdminEventPrice);
            tvRemaining = itemView.findViewById(R.id.tvAdminEventRemaining);
            btnEdit = itemView.findViewById(R.id.btnEditEvent);
            btnDelete = itemView.findViewById(R.id.btnDeleteEvent);
            cardForeground = itemView.findViewById(R.id.cardAdminEventForeground);
        }
    }
}
