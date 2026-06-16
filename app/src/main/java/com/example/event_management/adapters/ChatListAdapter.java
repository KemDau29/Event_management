package com.example.event_management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.event_management.R;
import java.util.List;
import java.util.Map;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<Map<String, Object>> chatList;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(String userId, String userName);
    }

    public ChatListAdapter(List<Map<String, Object>> chatList, OnChatClickListener listener) {
        this.chatList = chatList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Map<String, Object> chat = chatList.get(position);
        String name = (String) chat.get("fullname");
        String lastMessage = (String) chat.get("lastMessage");
        String avatarUrl = (String) chat.get("avatarUrl");
        
        holder.txtUserName.setText(name);
        holder.txtLastMessage.setText(lastMessage != null ? lastMessage : "Chưa có tin nhắn");

        if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
            if (avatarUrl.startsWith("http")) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(holder.imgAvatar);
            } else {
                try {
                    byte[] decodedString = android.util.Base64.decode(avatarUrl, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.imgAvatar.setImageBitmap(decodedBitmap);
                } catch (Exception e) {
                    holder.imgAvatar.setImageResource(R.drawable.ic_person);
                }
            }
        } else {
            holder.imgAvatar.setImageResource(R.drawable.ic_person);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick((String) chat.get("uid"), name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtLastMessage;
        android.widget.ImageView imgAvatar;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
