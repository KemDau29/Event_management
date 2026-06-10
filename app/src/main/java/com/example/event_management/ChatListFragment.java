package com.example.event_management;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.models.ChatGroup;
import com.example.event_management.models.FriendRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends Fragment {

    private EditText edtSearch;
    private ListView listChats;
    private List<String> chatList = new ArrayList<>();
    private List<String> chatIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseDatabase realtimeDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();

        edtSearch = view.findViewById(R.id.edtSearchUser);
        listChats = view.findViewById(R.id.listChats);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, chatList);
        listChats.setAdapter(adapter);

        view.findViewById(R.id.btnSearchUser).setOnClickListener(v -> searchUser());

        listChats.setOnItemClickListener((parent, v, position, id) -> {
            String targetId = chatIds.get(position);
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("receiverId", targetId);
            intent.putExtra("receiverName", chatList.get(position));
            startActivity(intent);
        });

        loadFriends();

        return view;
    }

    private void searchUser() {
        String name = edtSearch.getText().toString().trim();
        if (name.isEmpty()) return;

        db.collection("users").whereEqualTo("username", name).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Toast.makeText(getContext(), "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            } else {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String uId = doc.getId();
                    String uName = doc.getString("fullname");
                    if (uId.equals(mAuth.getUid())) continue;

                    new AlertDialog.Builder(getContext())
                            .setTitle("Kết bạn")
                            .setMessage("Gửi lời mời kết bạn đến " + uName + "?")
                            .setPositiveButton("Gửi", (dialog, which) -> sendFriendRequest(uId, uName))
                            .setNegativeButton("Hủy", null)
                            .show();
                }
            }
        });
    }

    private void sendFriendRequest(String toId, String toName) {
        String currentId = mAuth.getUid();
        // Get current user name first
        db.collection("users").document(currentId).get().addOnSuccessListener(doc -> {
            String fromName = doc.getString("fullname");
            FriendRequest request = new FriendRequest(currentId, fromName, toId, "pending");
            db.collection("friend_requests").add(request).addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Đã gửi lời mời kết bạn", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadFriends() {
        String currentId = mAuth.getUid();
        if (currentId == null) return;

        // For simplicity, let's assume friends are those in chats_realtime
        realtimeDb.getReference("friends").child(currentId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                chatIds.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String friendId = ds.getKey();
                    chatIds.add(friendId);
                    // Fetch name from Firestore
                    db.collection("users").document(friendId).get().addOnSuccessListener(doc -> {
                        chatList.add(doc.getString("fullname"));
                        adapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
