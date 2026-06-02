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

// ĐỔI TỪ BaseAdapter SANG RecyclerView.Adapter
public class UpcomingEventAdapter extends RecyclerView.Adapter<UpcomingEventAdapter.ViewHolder> {
    private final Context context;
    private List<Event> eventList = new ArrayList<>();
    private final OnItemClickListener listener; // Định nghĩa listener để truyền sự kiện click ra Fragment

    // Giao diện (Interface) hứng sự kiện click item từ HomeFragment truyền vào
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    // Cập nhật Constructor nhận 2 đối số (Context và Listener) khớp với HomeFragment của bạn
    public UpcomingEventAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp layout giao diện của từng item sự kiện sắp diễn ra
        View view = LayoutInflater.from(context).inflate(R.layout.item_upcoming_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        if (event != null) {
            holder.tvUpcomingTitle.setText(event.getTitle());
            holder.tvUpcomingDate.setText(event.getFormattedDate());
            holder.tvUpcomingPrice.setText(event.getPrice() == 0 ? "Miễn phí" : event.getPrice() + "đ");

            // Xử lý load ảnh bằng Glide
            if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
                Glide.with(context)
                        .load(event.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.imgUpcoming);
            } else {
                holder.imgUpcoming.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Xử lý sự kiện click vào item
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(event);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    // Lớp ViewHolder cải tiến giữ các liên kết View theo chuẩn RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUpcoming;
        TextView tvUpcomingTitle;
        TextView tvUpcomingDate;
        TextView tvUpcomingPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUpcoming = itemView.findViewById(R.id.imgUpcoming);
            tvUpcomingTitle = itemView.findViewById(R.id.tvUpcomingTitle);
            tvUpcomingDate = itemView.findViewById(R.id.tvUpcomingDate);
            tvUpcomingPrice = itemView.findViewById(R.id.tvUpcomingPrice);
        }
    }
}