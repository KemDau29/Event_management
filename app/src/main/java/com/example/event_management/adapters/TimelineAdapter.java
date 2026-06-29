package com.example.event_management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.event_management.R;
import com.example.event_management.models.TimelineItem;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {
    private List<TimelineItem> timelineItems;

    public TimelineAdapter(List<TimelineItem> timelineItems) {
        this.timelineItems = timelineItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_timeline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimelineItem item = timelineItems.get(position);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        String timeStr = "";
        if (item.getStartTime() != null && item.getEndTime() != null) {
            timeStr = sdf.format(item.getStartTime()) + " - " + sdf.format(item.getEndTime());
        } else if (item.getStartTime() != null) {
            timeStr = sdf.format(item.getStartTime());
        }
        
        holder.tvTime.setText(timeStr);
        holder.tvActivity.setText(item.getActivity());

        // Kiểm tra xem có đang diễn ra mốc này không (Sử dụng phép so sánh không khắt khe >= và <=)
        java.util.Date now = new java.util.Date();
        boolean isCurrent = false;
        if (item.getStartTime() != null && item.getEndTime() != null) {
            // Kiểm tra: startTime <= now <= endTime
            isCurrent = !now.before(item.getStartTime()) && !now.after(item.getEndTime());
        }

        if (isCurrent) {
            holder.tvTime.setTextColor(android.graphics.Color.parseColor("#E53935")); // Màu đỏ
            holder.tvActivity.setTextColor(android.graphics.Color.parseColor("#E53935"));
            holder.tvActivity.setTypeface(null, android.graphics.Typeface.BOLD);
            if (holder.dot != null) {
                holder.dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E53935")));
            }
        } else {
            holder.tvTime.setTextColor(android.graphics.Color.parseColor("#185FA5")); // Màu xanh mặc định
            holder.tvActivity.setTextColor(android.graphics.Color.parseColor("#333333"));
            holder.tvActivity.setTypeface(null, android.graphics.Typeface.NORMAL);
            if (holder.dot != null) {
                holder.dot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#185FA5")));
            }
        }

        // Ẩn line top cho item đầu và line bottom cho item cuối
        holder.viewLineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.viewLineBottom.setVisibility(position == timelineItems.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return timelineItems != null ? timelineItems.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvActivity;
        View viewLineTop, viewLineBottom, dot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTimelineTime);
            tvActivity = itemView.findViewById(R.id.tvTimelineActivity);
            viewLineTop = itemView.findViewById(R.id.viewLineTop);
            viewLineBottom = itemView.findViewById(R.id.viewLineBottom);
            dot = itemView.findViewById(R.id.viewTimelineDot);
        }
    }
}

// Cần cập nhật item_timeline.xml để có id viewTimelineDot
