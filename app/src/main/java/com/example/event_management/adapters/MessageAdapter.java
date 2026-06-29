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
    private static final int TYPE_SENT_EVENT = 3;
    private static final int TYPE_RECEIVED_EVENT = 4;

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        boolean isSent = message.getSenderId().equals(currentUserId);
        if (message.isEventShare()) {
            return isSent ? TYPE_SENT_EVENT : TYPE_RECEIVED_EVENT;
        } else {
            return isSent ? TYPE_SENT : TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
        } else if (viewType == TYPE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
        } else if (viewType == TYPE_SENT_EVENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_event_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_event_received, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        
        if (message.isEventShare()) {
            holder.tvTitle.setText(message.getEventTitle());
            holder.tvDate.setText("🕒 " + message.getEventDate());
            holder.tvPrice.setText(String.format(java.util.Locale.getDefault(), "%,dđ", message.getEventPrice()));
            
            if (message.getEventImageUrl() != null && !message.getEventImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(message.getEventImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(holder.imgEvent);
            } else {
                holder.imgEvent.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            View.OnClickListener clickListener = v -> {
                if (message.getEventId() == null) return;

                // Fetch full event object to open detail
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("events").document(message.getEventId())
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                com.example.event_management.models.Event event = documentSnapshot.toObject(com.example.event_management.models.Event.class);
                                if (event != null) {
                                    event.setId(documentSnapshot.getId()); // Cực kỳ quan trọng: Gán ID cho sự kiện
                                    android.content.Intent intent = new android.content.Intent(holder.itemView.getContext(), com.example.event_management.EventDetailActivity.class);
                                    intent.putExtra("event", event);
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            }
                        });
            };

            if (holder.cardEventShare != null) {
                holder.cardEventShare.setOnClickListener(clickListener);
            }
            holder.itemView.setOnClickListener(clickListener);
        } else {
            if (holder.tvMessage != null) {
                holder.tvMessage.setText(message.getMessage());
            }
            // Reset click listeners for text messages (due to view recycling)
            if (holder.cardEventShare != null) {
                holder.cardEventShare.setOnClickListener(null);
            }
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        // Event share views
        TextView tvTitle, tvDate, tvPrice;
        android.widget.ImageView imgEvent;
        androidx.cardview.widget.CardView cardEventShare;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessageContent);
            tvTitle = itemView.findViewById(R.id.tvEventShareTitle);
            tvDate = itemView.findViewById(R.id.tvEventShareDate);
            tvPrice = itemView.findViewById(R.id.tvEventSharePrice);
            imgEvent = itemView.findViewById(R.id.imgEventShare);
            cardEventShare = itemView.findViewById(R.id.cardEventShare);
        }
    }
}
