package com.example.event_management.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.R;
import com.example.event_management.models.CartItem;
import com.example.event_management.models.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminUserDetailFragment extends Fragment {

    private String userId;
    private FirebaseFirestore db;
    private TextView tvName, tvInfo;
    private ListView listUserEvents;
    private List<String> userEvents = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user_detail, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        db = FirebaseFirestore.getInstance();
        tvName = view.findViewById(R.id.tvAdminUserDetailName);
        tvInfo = view.findViewById(R.id.tvAdminUserDetailInfo);
        listUserEvents = view.findViewById(R.id.listUserEvents);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, userEvents);
        listUserEvents.setAdapter(adapter);

        loadUserInfo();
        loadUserEvents();

        return view;
    }

    private void loadUserInfo() {
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String fullname = documentSnapshot.getString("fullname");
                String email = documentSnapshot.getString("email");
                String username = documentSnapshot.getString("username");
                String role = documentSnapshot.getString("role");

                tvName.setText(fullname);
                tvInfo.setText("Email: " + email + "\nUsername: " + username + "\nRole: " + role);
            }
        });
    }

    private void loadUserEvents() {
        db.collection("orders").whereEqualTo("userId", userId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            Set<String> uniqueEvents = new HashSet<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                if (order.getItems() != null) {
                    for (CartItem item : order.getItems()) {
                        uniqueEvents.add(item.getTitle() + " (x" + item.getQuantity() + ")");
                    }
                }
            }
            userEvents.clear();
            userEvents.addAll(uniqueEvents);
            if (userEvents.isEmpty()) {
                userEvents.add("Chưa tham gia sự kiện nào");
            }
            adapter.notifyDataSetChanged();
        });
    }
}
