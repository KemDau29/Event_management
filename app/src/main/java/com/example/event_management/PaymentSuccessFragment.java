package com.example.event_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.event_management.adapters.EventAdapter;
import com.example.event_management.models.CartItem;
import com.example.event_management.models.Event;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PaymentSuccessFragment extends Fragment {

    private static final String ARG_PURCHASED_ITEMS = "purchased_items";
    private List<CartItem> purchasedItems;
    private FirebaseFirestore db;
    private EventAdapter adapter;

    public static PaymentSuccessFragment newInstance(List<CartItem> items) {
        PaymentSuccessFragment fragment = new PaymentSuccessFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PURCHASED_ITEMS, (ArrayList<CartItem>) items);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            purchasedItems = (List<CartItem>) getArguments().getSerializable(ARG_PURCHASED_ITEMS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_success, container, false);
        
        db = FirebaseFirestore.getInstance();
        ListView listRelated = view.findViewById(R.id.listRelatedEvents);
        adapter = new EventAdapter(requireContext());
        listRelated.setAdapter(adapter);

        view.findViewById(R.id.btnBackHome).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });

        loadRelatedEvents();

        listRelated.setOnItemClickListener((parent, v, position, id) -> {
            Event event = (Event) adapter.getItem(position);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, event_detail.newInstance(event))
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadRelatedEvents() {
        if (purchasedItems == null || purchasedItems.isEmpty()) return;

        // Lấy danh sách ID các sự kiện đã mua để loại trừ
        List<String> purchasedIds = new ArrayList<>();
        for (CartItem item : purchasedItems) {
            purchasedIds.add(item.getEventId());
        }

        // Lấy Category của sản phẩm đầu tiên đã mua để làm mẫu gợi ý
        String firstEventId = purchasedItems.get(0).getEventId();
        
        db.collection("events").document(firstEventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                DocumentReference cateRef = documentSnapshot.getDocumentReference("cate");
                if (cateRef != null) {
                    // Tìm các sự kiện có cùng cateRef
                    db.collection("events")
                            .whereEqualTo("cate", cateRef)
                            .limit(10)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                List<Event> relatedList = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                    if (!purchasedIds.contains(doc.getId())) {
                                        Event event = doc.toObject(Event.class);
                                        event.setId(doc.getId());
                                        relatedList.add(event);
                                    }
                                }
                                adapter.setEventList(relatedList);
                            });
                }
            }
        });
    }
}
