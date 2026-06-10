package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.event_management.R;
import com.example.event_management.models.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminEventAdapter extends BaseAdapter {
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_admin_event, parent, false);
            holder = new ViewHolder();
            holder.img = convertView.findViewById(R.id.imgAdminEvent);
            holder.tvName = convertView.findViewById(R.id.tvAdminEventName);
            holder.tvPrice = convertView.findViewById(R.id.tvAdminEventPrice);
            holder.btnEdit = convertView.findViewById(R.id.btnEditEvent);
            holder.btnDelete = convertView.findViewById(R.id.btnDeleteEvent);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = eventList.get(position);
        if (event != null) {
            holder.tvName.setText(event.getTitle());
            holder.tvPrice.setText(String.format(Locale.getDefault(), "%,dđ", event.getPrice()));
            
            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(context).load(event.getImageUrl()).into(holder.img);
            } else {
                holder.img.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            holder.btnEdit.setOnClickListener(v -> listener.onEdit(event));
            holder.btnDelete.setOnClickListener(v -> listener.onDelete(event));
            convertView.setOnClickListener(v -> listener.onDetail(event));
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView img, btnEdit, btnDelete;
        TextView tvName, tvPrice;
    }
}
