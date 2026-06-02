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
import com.example.event_management.adapters.TicketAdapter;
import com.example.event_management.models.Ticket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MyTicketsFragment extends Fragment {

    private TextView tabPurchased, tabSold, tabCancelled;
    private View indicator;
    private ListView listTickets;
    private TicketAdapter adapter;
    private List<Ticket> ticketList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentStatus = "Đã mua";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_tickets, container, false);

        tabPurchased = view.findViewById(R.id.tabPurchased);
        tabSold = view.findViewById(R.id.tabSold);
        tabCancelled = view.findViewById(R.id.tabCancelled);
        indicator = view.findViewById(R.id.indicator);
        listTickets = view.findViewById(R.id.listTickets);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        adapter = new TicketAdapter(requireContext());
        listTickets.setAdapter(adapter);

        view.findViewById(R.id.btnBackMyTickets).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        tabPurchased.setOnClickListener(v -> selectTab("Đã mua"));
        tabSold.setOnClickListener(v -> selectTab("Đã bán"));
        tabCancelled.setOnClickListener(v -> selectTab("Đã hủy"));

        if (mAuth.getCurrentUser() != null) {
            loadTickets();
        }

        return view;
    }

    private void selectTab(String status) {
        currentStatus = status;
        
        // Update UI
        tabPurchased.setTextColor(status.equals("Đã mua") ? 0xFFFFFFFF : 0xFF888888);
        tabSold.setTextColor(status.equals("Đã bán") ? 0xFFFFFFFF : 0xFF888888);
        tabCancelled.setTextColor(status.equals("Đã hủy") ? 0xFFFFFFFF : 0xFF888888);
        
        // Move indicator (simple animation or just jump)
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float translationX = 0;
        if (status.equals("Đã mua")) translationX = 0;
        else if (status.equals("Đã bán")) translationX = screenWidth / 3f;
        else if (status.equals("Đã hủy")) translationX = (screenWidth / 3f) * 2;
        
        indicator.animate().translationX(translationX).setDuration(200).start();

        loadTickets();
    }

    private void loadTickets() {
        String uid = mAuth.getCurrentUser().getUid();
        
        db.collection("tickets")
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", currentStatus)
                .orderBy("purchaseDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        ticketList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            ticketList.add(doc.toObject(Ticket.class));
                        }
                        adapter.setTicketList(ticketList);
                    }
                });
    }
}
