package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView; // Đã thêm import
import android.widget.TextView;
import com.bumptech.glide.Glide; // Đã thêm thư viện Glide để load ảnh
import com.example.event_management.R;
import com.example.event_management.models.Event;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends BaseAdapter {
    private final Context context;
    private List<Event> eventList = new ArrayList<>();

    public EventAdapter(Context context) {
        this.context = context;
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return eventList.size(); }

    @Override
    public Object getItem(int position) { return eventList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // 1. Chỉ inflate giao diện khi thực sự cần thiết (View mới tinh)
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_event, parent, false); //

            // 2. Tạo một cái rổ (Holder) để gom và giữ cố định các ánh xạ ID
            holder = new ViewHolder();
            holder.tvName = convertView.findViewById(R.id.tvEventName); //
            holder.tvDate = convertView.findViewById(R.id.tvEventDate); //
            holder.tvLocation = convertView.findViewById(R.id.tvEventLocation); //
            holder.tvPrice = convertView.findViewById(R.id.tvEventPrice); //
            holder.imgEvent = convertView.findViewById(R.id.imgEvent); //
            holder.tvSoldOutLabel = convertView.findViewById(R.id.tvSoldOutLabel);

            // Gắn chiếc rổ này vào convertView để lần sau dùng lại
            convertView.setTag(holder); //
        } else {
            // 3. Nếu View cũ được tái sử dụng, chỉ cần lấy lại cái rổ đã có sẵn
            holder = (ViewHolder) convertView.getTag(); //
        }

        // 4. Đổ dữ liệu an toàn vào các View thông qua holder
        Event event = eventList.get(position); //

        if (event != null) {
            holder.tvName.setText(event.getTitle()); //
            holder.tvDate.setText(event.getFormattedDate()); //
            holder.tvLocation.setText(event.getLocation()); //

            // Xử lý hiển thị hết vé
            if (event.isLimited() && event.getRemainingTickets() <= 0) {
                holder.tvSoldOutLabel.setVisibility(View.VISIBLE);
                holder.tvPrice.setTextColor(android.graphics.Color.GRAY);
            } else {
                holder.tvSoldOutLabel.setVisibility(View.GONE);
                holder.tvPrice.setTextColor(android.graphics.Color.BLACK);
            }

            // Xử lý hiển thị giá tiền
            holder.tvPrice.setText(event.getPrice() == 0 ? "Miễn phí" : event.getPrice() + "đ"); //
            android.util.Log.d("KiemTraAnh", "Ten Event: " + event.getTitle() + " | Link Anh: " + event.getImageUrl());
            // 🔥 ĐOẠN CODE HOÀN CHỈNH ĐỂ HIỂN THỊ HÌNH ẢNH QUA GLIDE:
            if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
                Glide.with(context)
                        .load(event.getImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery) // Ảnh hiển thị tạm khi đang tải
                        .error(android.R.drawable.stat_notify_error)       // Ảnh hiển thị nếu link bị lỗi
                        .into(holder.imgEvent);
            } else {
                // Nếu không có link ảnh hoặc bị trống, đặt ảnh hệ thống mặc định
                holder.imgEvent.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        return convertView;
    }

    // Lớp bọc tĩnh lưu trữ View để tối ưu hiệu năng cuộn ứng dụng
    static class ViewHolder {
        TextView tvName; //
        TextView tvDate; //
        TextView tvLocation; //
        TextView tvPrice; //
        ImageView imgEvent; //
        TextView tvSoldOutLabel;
    }
}