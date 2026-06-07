package com.example.eventmanagementsystem.service;

import com.example.eventmanagementsystem.model.ChatMessage;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatService {
    private DatabaseReference mDatabase;

    public ChatService() {
        mDatabase = FirebaseDatabase.getInstance().getReference("chats");
    }

    public void sendMessage(String senderId, String receiverId, String message) {
        String chatId = getChatId(senderId, receiverId);
        String messageId = mDatabase.child(chatId).push().getKey();
        ChatMessage chatMessage = new ChatMessage(senderId, receiverId, message, System.currentTimeMillis());
        chatMessage.setId(messageId);
        if (messageId != null) {
            mDatabase.child(chatId).child(messageId).setValue(chatMessage);
        }
    }

    public void listenForMessages(String senderId, String receiverId, ValueEventListener listener) {
        String chatId = getChatId(senderId, receiverId);
        mDatabase.child(chatId).addValueEventListener(listener);
    }

    private String getChatId(String id1, String id2) {
        // Ensure same chat ID regardless of who sends first
        if (id1.compareTo(id2) < 0) {
            return id1 + "_" + id2;
        } else {
            return id2 + "_" + id1;
        }
    }
    
    public void sendMessageToAdmin(String userId, String message) {
        sendMessage(userId, "admin", message);
    }
}
