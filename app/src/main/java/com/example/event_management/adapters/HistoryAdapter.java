package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.event_management.R;
import com.example.event_management.models.CartItem;
import com.example.event_management.models.Order;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends BaseAdapter {
    private Context context;
    private List<Order> orderList = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(Context context) {
        this.context = context;
    }

    public void setOrderList(List<Order> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return orderList.size(); }

    @Override
    public Object getItem(int position) { return orderList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_history, parent, false);
            holder = new ViewHolder();
            holder.tvOrderId = convertView.findViewById(R.id.tvHistoryOrderId);
            holder.tvDate = convertView.findViewById(R.id.tvHistoryDate);
            holder.tvTotalPrice = convertView.findViewById(R.id.tvHistoryTotalPrice);
            holder.layoutItems = convertView.findViewById(R.id.layoutHistoryItems);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Order order = orderList.get(position);
        if (order != null) {
            String shortId = order.getOrderId();
            if (shortId != null && shortId.length() > 8) {
                shortId = shortId.substring(0, 8);
            }
            holder.tvOrderId.setText(String.format("Mã đơn: #%s", (shortId != null ? shortId.toUpperCase() : "N/A")));
            holder.tvDate.setText(order.getTimestamp() != null ? sdf.format(order.getTimestamp()) : "Ngày không xác định");
            holder.tvTotalPrice.setText(String.format(Locale.getDefault(), "Tổng: %dđ", order.getTotalPrice()));

            // Hiển thị danh sách các sự kiện trong đơn hàng
            holder.layoutItems.removeAllViews();
            List<CartItem> purchasedItems = order.getItems();
            if (purchasedItems != null && !purchasedItems.isEmpty()) {
                for (CartItem item : purchasedItems) {
                    // Inflate layout đơn giản cho mỗi mục trong đơn hàng
                    View itemView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, holder.layoutItems, false);
                    TextView text1 = itemView.findViewById(android.R.id.text1);
                    TextView text2 = itemView.findViewById(android.R.id.text2);
                    
                    text1.setText(item.getTitle());
                    text1.setTextColor(context.getResources().getColor(android.R.color.black));
                    text1.setTextSize(14);
                    
                    text2.setText(String.format(Locale.getDefault(), "Số lượng: %d | Giá: %dđ", item.getQuantity(), item.getPrice()));
                    text2.setTextSize(12);
                    
                    holder.layoutItems.addView(itemView);
                }
            } else {
                TextView tvEmpty = new TextView(context);
                tvEmpty.setText("Không có chi tiết sản phẩm");
                holder.layoutItems.addView(tvEmpty);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        TextView tvOrderId, tvDate, tvTotalPrice;
        LinearLayout layoutItems;
    }
}
