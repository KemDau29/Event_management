package com.example.event_management;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.event_management.adapters.MessageAdapter;
import com.example.event_management.models.Message;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private String receiverId, receiverName, senderId;
    private RecyclerView recyclerView;
    private EditText edtMessage;
    private ImageButton btnSend;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverId = getIntent().getStringExtra("receiverId");
        receiverName = getIntent().getStringExtra("receiverName");
        senderId = FirebaseAuth.getInstance().getUid();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(receiverName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerMessages);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSendMessage);
        
        TextView tvTitle = findViewById(R.id.tvChatReceiverName);
        ImageView imgReceiver = findViewById(R.id.imgChatReceiverAvatar);
        
        tvTitle.setText(receiverName);
        loadReceiverAvatar(imgReceiver);

        adapter = new MessageAdapter(messageList, senderId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private String getChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    private void sendMessage() {
        String msgText = edtMessage.getText().toString().trim();
        if (msgText.isEmpty()) return;

        String chatId = getChatId(senderId, receiverId);
        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderId);
        message.put("receiverId", receiverId);
        message.put("message", msgText);
        message.put("timestamp", System.currentTimeMillis());

        edtMessage.setText("");

        db.collection("chats").document(chatId).collection("messages").add(message);
    }

    private void loadMessages() {
        String chatId = getChatId(senderId, receiverId);
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        messageList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Message msg = doc.toObject(Message.class);
                            messageList.add(msg);
                        }
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            recyclerView.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    private void loadReceiverAvatar(ImageView imageView) {
        db.collection("users").document(receiverId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String avatarUrl = documentSnapshot.getString("avatarUrl");
                if (avatarUrl != null && !avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
                    if (avatarUrl.startsWith("http")) {
                        Glide.with(this).load(avatarUrl).placeholder(R.drawable.ic_person).into(imageView);
                    } else {
                        try {
                            byte[] decodedString = android.util.Base64.decode(avatarUrl, android.util.Base64.DEFAULT);
                            android.graphics.Bitmap decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            imageView.setImageBitmap(decodedBitmap);
                        } catch (Exception e) {
                            imageView.setImageResource(R.drawable.ic_person);
                        }
                    }
                } else {
                    imageView.setImageResource(R.drawable.ic_person);
                }
            }
        });
    }
}
