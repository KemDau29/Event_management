package com.example.event_management;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.event_management.adapters.ChatListAdapter;
import com.example.event_management.adapters.UserSearchAdapter;
import com.example.event_management.models.FriendRequest;
import com.example.event_management.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListFragment extends Fragment {

    private EditText edtSearch;
    private RecyclerView recyclerView;
    private List<Map<String, Object>> chatList = new ArrayList<>();
    private List<Map<String, Object>> searchResults = new ArrayList<>();
    private ChatListAdapter chatAdapter;
    private UserSearchAdapter searchAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseDatabase realtimeDb;
    private boolean isSearching = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();

        edtSearch = view.findViewById(R.id.edtSearchUser);
        recyclerView = view.findViewById(R.id.listChats);

        chatAdapter = new ChatListAdapter(chatList, (userId, userName) -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("receiverId", userId);
            intent.putExtra("receiverName", userName);
            startActivity(intent);
        });

        searchAdapter = new UserSearchAdapter(getContext(), searchResults, this::sendFriendRequest);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatAdapter);

        view.findViewById(R.id.btnNotification).setOnClickListener(v -> {
            // Clear search before going to notifications to ensure chat list is shown on return
            edtSearch.setText("");
            isSearching = false;
            recyclerView.setAdapter(chatAdapter);

            Intent intent = new Intent(getContext(), FriendRequestActivity.class);
            startActivity(intent);
        });

        setupSearch();
        loadFriends();

        return view;
    }

    private void setupSearch() {
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchUser(edtSearch.getText().toString().trim());
                return true;
            }
            return false;
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    isSearching = false;
                    recyclerView.setAdapter(chatAdapter);
                    chatAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchUser(String query) {
        if (query.isEmpty()) return;
        isSearching = true;
        db.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    searchResults.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        if (doc.getId().equals(mAuth.getUid())) continue;
                        Map<String, Object> user = new HashMap<>();
                        user.put("uid", doc.getId());
                        user.put("fullname", doc.getString("fullname"));
                        user.put("username", doc.getString("username"));
                        searchResults.add(user);
                    }
                    recyclerView.setAdapter(searchAdapter);
                    searchAdapter.notifyDataSetChanged();
                });
    }

    private void sendFriendRequest(String toId, String toName) {
        String currentId = mAuth.getUid();
        if (currentId == null) return;
        db.collection("users").document(currentId).get().addOnSuccessListener(doc -> {
            String fromName = doc.getString("fullname");
            FriendRequest request = new FriendRequest(currentId, fromName, toId, "pending");
            db.collection("friend_requests")
                    .whereEqualTo("fromId", currentId).whereEqualTo("toId", toId).get()
                    .addOnSuccessListener(qs -> {
                        if (qs.isEmpty()) {
                            db.collection("friend_requests").add(request).addOnSuccessListener(a ->
                                    Toast.makeText(getContext(), "Đã gửi lời mời", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(getContext(), "Đã gửi lời mời trước đó", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loadFriends() {
        String currentId = mAuth.getUid();
        if (currentId == null) return;

        // Listen for friends where current user is user1
        db.collection("friends")
                .whereEqualTo("user1", currentId)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            fetchFriendAndLastMessage(doc.getString("user2"));
                        }
                    }
                });

        // Listen for friends where current user is user2
        db.collection("friends")
                .whereEqualTo("user2", currentId)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            fetchFriendAndLastMessage(doc.getString("user1"));
                        }
                    }
                });
    }

    private void fetchFriendAndLastMessage(String friendId) {
        db.collection("users").document(friendId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Map<String, Object> friendMap = new HashMap<>();
                friendMap.put("uid", friendId);
                friendMap.put("fullname", doc.getString("fullname"));
                friendMap.put("avatarUrl", doc.getString("avatarUrl"));
                friendMap.put("timestamp", 0L);
                friendMap.put("lastMessage", "Bắt đầu trò chuyện ngay");

                // Avoid duplicates if already exists
                boolean exists = false;
                for (int i = 0; i < chatList.size(); i++) {
                    if (chatList.get(i).get("uid").equals(friendId)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    chatList.add(friendMap);
                    if (!isSearching) sortAndNotify();
                    listenToLastMessage(friendId, friendMap);
                }
            }
        });
    }

    private void listenToLastMessage(String friendId, Map<String, Object> friendMap) {
        String chatKey = getChatKey(mAuth.getUid(), friendId);
        realtimeDb.getReference("chats").child(chatKey).limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Message msg = ds.getValue(Message.class);
                        if (msg != null) {
                            friendMap.put("lastMessage", msg.getMessage());
                            friendMap.put("timestamp", msg.getTimestamp());
                        }
                    }
                }
                if (!isSearching) {
                    sortAndNotify();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getChatKey(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    private void sortAndNotify() {
        Collections.sort(chatList, (o1, o2) -> {
            Long t1 = (Long) o1.get("timestamp");
            Long t2 = (Long) o2.get("timestamp");
            return t2.compareTo(t1);
        });
        chatAdapter.notifyDataSetChanged();
    }
}