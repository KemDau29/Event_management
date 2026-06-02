package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.example.event_management.R;
import com.example.event_management.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends BaseAdapter {
    private final Context context;
    private List<CartItem> cartItemList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OnCartItemChangeListener changeListener;

    // Interface callback để báo cho Fragment biết khi có thay đổi trạng thái
    public interface OnCartItemChangeListener {
        void onCartItemChanged();
    }

    public CartAdapter(Context context, OnCartItemChangeListener listener) {
        this.context = context;
        this.changeListener = listener;
    }

    public void setCartItemList(List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return cartItemList.size(); }

    @Override
    public Object getItem(int position) { return cartItemList.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
            holder = new ViewHolder();
            holder.cbSelect = convertView.findViewById(R.id.cbSelectCartItem);
            holder.imgEvent = convertView.findViewById(R.id.imgCartEvent);
            holder.tvName = convertView.findViewById(R.id.tvCartEventName);
            holder.tvDate = convertView.findViewById(R.id.tvCartEventDate);
            holder.tvLocation = convertView.findViewById(R.id.tvCartEventLocation);
            holder.tvPrice = convertView.findViewById(R.id.tvCartEventPrice);
            holder.tvQty = convertView.findViewById(R.id.tvCartEventQty);
            holder.btnDecrease = convertView.findViewById(R.id.btnDecreaseQty);
            holder.btnIncrease = convertView.findViewById(R.id.btnIncreaseQty);
            holder.btnRemove = convertView.findViewById(R.id.btnRemoveCartItem);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CartItem item = cartItemList.get(position);
        if (item != null) {
            holder.tvName.setText(item.getTitle());
            holder.tvDate.setText(item.getFormattedDate());
            holder.tvLocation.setText(item.getLocation());
            holder.tvPrice.setText(String.format(java.util.Locale.getDefault(), "%dđ", item.getPrice()));
            holder.tvQty.setText(String.valueOf(item.getQuantity()));

            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(context)
                        .load(imageUrl.trim())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.stat_notify_error)
                        .into(holder.imgEvent);
            } else {
                holder.imgEvent.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            if (FirebaseAuth.getInstance().getCurrentUser() == null) return convertView;
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Reset sự kiện CheckBox để tránh lỗi vòng lặp giao diện
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(item.isChosen());

            // Lắng nghe sự kiện tick chọn mặt hàng
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChosen(isChecked);
                if (changeListener != null) changeListener.onCartItemChanged();

                db.collection("carts").document(uid)
                        .collection("cart_items").document(item.getEventId())
                        .update("isChosen", isChecked);
            });

            // Nút giảm số lượng
            holder.btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    int newQty = item.getQuantity() - 1;
                    item.setQuantity(newQty);
                    holder.tvQty.setText(String.valueOf(newQty));
                    if (changeListener != null) changeListener.onCartItemChanged();

                    db.collection("carts").document(uid)
                            .collection("cart_items").document(item.getEventId())
                            .update("quantity", newQty);
                }
            });

            // Nút tăng số lượng
            holder.btnIncrease.setOnClickListener(v -> {
                int newQty = item.getQuantity() + 1;
                item.setQuantity(newQty);
                holder.tvQty.setText(String.valueOf(newQty));
                if (changeListener != null) changeListener.onCartItemChanged();

                db.collection("carts").document(uid)
                        .collection("cart_items").document(item.getEventId())
                        .update("quantity", newQty);
            });

            // Nút xóa mặt hàng khỏi giỏ
            holder.btnRemove.setOnClickListener(v -> {
                db.collection("carts").document(uid)
                        .collection("cart_items").document(item.getEventId())
                        .delete();
            });
        }

        return convertView;
    }

    static class ViewHolder {
        CheckBox cbSelect;
        ImageView imgEvent;
        TextView tvName, tvDate, tvLocation, tvPrice, tvQty, btnDecrease, btnIncrease;
        ImageView btnRemove;
    }
}