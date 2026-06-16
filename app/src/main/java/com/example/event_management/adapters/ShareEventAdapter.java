package com.example.event_management.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.event_management.R;
import java.util.List;
import java.util.Map;

public class ShareEventAdapter extends RecyclerView.Adapter<ShareEventAdapter.FriendViewHolder> {
    private Context context;
    private List<Map<String, Object>> friendList;
    private OnFriendSelectListener listener;

    public interface OnFriendSelectListener {
        void onFriendSelected(String friendId, String friendName);
    }

    public ShareEventAdapter(Context context, List<Map<String, Object>> friendList, OnFriendSelectListener listener) {
        this.context = context;
        this.friendList = friendList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_share_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Map<String, Object> friend = friendList.get(position);
        String uid = (String) friend.get("uid");
        String fullName = (String) friend.get("fullname");
        String avatarUrl = (String) friend.get("avatarUrl");

        holder.tvFriendName.setText(fullName);
        
        if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
            if (avatarUrl.startsWith("http")) {
                // Nếu là URL (Firebase Storage hoặc link web)
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(holder.imgAvatar);
            } else {
                // Nếu là chuỗi Base64
                try {
                    byte[] decodedString = android.util.Base64.decode(avatarUrl, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    if (decodedBitmap != null) {
                        holder.imgAvatar.setImageBitmap(decodedBitmap);
                    } else {
                        holder.imgAvatar.setImageResource(R.drawable.ic_person);
                    }
                } catch (Exception e) {
                    holder.imgAvatar.setImageResource(R.drawable.ic_person);
                }
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_person);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendSelected(uid, fullName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName;
        ImageView imgAvatar;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            imgAvatar = itemView.findViewById(R.id.imgShareFriendAvatar);
        }
    }
}
