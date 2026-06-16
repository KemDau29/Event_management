package com.example.event_management;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.event_management.adapters.FriendRequestAdapter;
import com.example.event_management.models.FriendRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendRequestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FriendRequestAdapter adapter;
    private List<FriendRequest> requestList = new ArrayList<>();
    private List<String> requestIdList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerFriendRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FriendRequestAdapter(requestList, new FriendRequestAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(FriendRequest request) {
                acceptRequest(request, requestIdList.get(requestList.indexOf(request)));
            }

            @Override
            public void onDecline(FriendRequest request) {
                declineRequest(requestIdList.get(requestList.indexOf(request)));
            }
        });
        recyclerView.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        String currentId = mAuth.getUid();
        db.collection("friend_requests")
                .whereEqualTo("toId", currentId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        requestList.clear();
                        requestIdList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            requestList.add(doc.toObject(FriendRequest.class));
                            requestIdList.add(doc.getId());
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void acceptRequest(FriendRequest request, String requestId) {
        String currentId = mAuth.getUid();
        if (currentId == null) return;

        // 1. Cập nhật trạng thái trong Firestore
        db.collection("friend_requests").document(requestId)
                .update("status", "accepted")
                .addOnSuccessListener(aVoid -> {
                    // 2. Lưu quan hệ bạn bè vào Firestore collection "friends"
                    Map<String, Object> friendship = new HashMap<>();
                    friendship.put("user1", currentId);
                    friendship.put("user2", request.getFromId());
                    friendship.put("timestamp", System.currentTimeMillis());

                    db.collection("friends").add(friendship)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(this, "Đã kết bạn thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi khi tạo quan hệ bạn bè", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi cập nhật lời mời", Toast.LENGTH_SHORT).show();
                });
    }

    private void declineRequest(String requestId) {
        db.collection("friend_requests").document(requestId).update("status", "rejected")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã từ chối lời mời", Toast.LENGTH_SHORT).show();
                });
    }
}
