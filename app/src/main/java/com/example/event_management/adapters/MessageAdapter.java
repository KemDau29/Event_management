package com.example.event_management.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.event_management.R;
import com.example.event_management.models.Message;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.tvMessage.setText(message.getMessage());

        if (message.isEventShare() && message.getEventId() != null) {
            holder.tvMessage.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.white));
            holder.tvMessage.setOnClickListener(v -> {
                // Fetch event from Firestore then open detail
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("events").document(message.getEventId())
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                com.example.event_management.models.Event event = documentSnapshot.toObject(com.example.event_management.models.Event.class);
                                if (event != null && holder.itemView.getContext() instanceof androidx.fragment.app.FragmentActivity) {
                                    androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) holder.itemView.getContext();
                                    com.example.event_management.event_detail detailFragment = com.example.event_management.event_detail.newInstance(event);
                                    activity.getSupportFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, detailFragment)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            } else {
                                android.widget.Toast.makeText(holder.itemView.getContext(), "Sự kiện không còn tồn tại", android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
            });
            // Optional: add visual hint that it's clickable
            holder.tvMessage.setPaintFlags(holder.tvMessage.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        } else {
            holder.tvMessage.setOnClickListener(null);
            holder.tvMessage.setPaintFlags(holder.tvMessage.getPaintFlags() & (~android.graphics.Paint.UNDERLINE_TEXT_FLAG));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessageContent);
        }
    }
}
