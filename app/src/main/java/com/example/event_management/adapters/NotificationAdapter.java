package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.event_management.R;
import com.example.event_management.models.Notification;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends BaseAdapter {
    private Context context;
    private List<Notification> notificationList = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public NotificationAdapter(Context context) {
        this.context = context;
    }

    public void setNotificationList(List<Notification> notifications) {
        this.notificationList = notifications;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }

    @Override
    public Object getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.tvNotificationItemTitle);
            holder.tvMessage = convertView.findViewById(R.id.tvNotificationItemMessage);
            holder.tvTimestamp = convertView.findViewById(R.id.tvNotificationItemTimestamp);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Thông báo mới");
        holder.tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "Xem chi tiết để biết thêm.");
        holder.tvTimestamp.setText(formatDate(notification.getTimestamp()));

        return convertView;
    }

    private String formatDate(Date date) {
        if (date == null) return "";
        return sdf.format(date);
    }

    static class ViewHolder {
        TextView tvTitle;
        TextView tvMessage;
        TextView tvTimestamp;
    }
}
