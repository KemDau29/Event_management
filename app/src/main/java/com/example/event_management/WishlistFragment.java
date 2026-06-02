package com.example.event_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

// SỬ DỤNG LẠI EVENTADAPTER
import com.example.event_management.adapters.EventAdapter;
import com.example.event_management.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class WishlistFragment extends Fragment {

    private EventAdapter adapter; // Đổi lại thành EventAdapter
    private List<Event> wishlistItems;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ListView listWishlist = view.findViewById(R.id.listWishlist);
        wishlistItems = new ArrayList<>();

        // Khởi tạo EventAdapter
        adapter = new EventAdapter(requireContext());
        listWishlist.setAdapter(adapter);

        if (mAuth.getCurrentUser() != null) {
            loadWishlist();
        }

        View btnBack = view.findViewById(R.id.btnBackWishlist);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                getParentFragmentManager().popBackStack();
            });
        }

        listWishlist.setOnItemClickListener((parent, v, position, id) -> {
            Event event = (Event) adapter.getItem(position);
            if (event != null) {
                event_detail detailFragment = event_detail.newInstance(event);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void loadWishlist() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("wishlists").document(uid)
                .collection("items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        wishlistItems.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            wishlistItems.add(event);
                        }
                        adapter.setEventList(wishlistItems);
                    }
                });
    }
}