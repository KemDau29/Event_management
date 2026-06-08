package com.example.event_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.adapters.NotificationAdapter;
import com.example.event_management.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private ListView listNotifications;
    private TextView tvNoNotifications;
    private NotificationAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Notification> notificationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        listNotifications = view.findViewById(R.id.listNotifications);
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        adapter = new NotificationAdapter(requireContext());
        listNotifications.setAdapter(adapter);

        listNotifications.setOnItemClickListener((parent, itemView, position, id) -> {
            Notification notification = notificationList.get(position);
            NotificationDetailFragment detailFragment = NotificationDetailFragment.newInstance(notification);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadNotifications();
        return view;
    }

    private void loadNotifications() {
        if (mAuth.getCurrentUser() == null) {
            showEmptyState();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users")
                .document(uid)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = doc.toObject(Notification.class);
                        notificationList.add(notification);
                    }
                    Collections.sort(notificationList, Comparator.comparing(Notification::getTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));
                    if (notificationList.isEmpty()) {
                        showEmptyState();
                    } else {
                        tvNoNotifications.setVisibility(View.GONE);
                        adapter.setNotificationList(notificationList);
                    }
                })
                .addOnFailureListener(e -> showEmptyState());
    }

    private void showEmptyState() {
        notificationList.clear();
        adapter.setNotificationList(notificationList);
        tvNoNotifications.setVisibility(View.VISIBLE);
    }
}
