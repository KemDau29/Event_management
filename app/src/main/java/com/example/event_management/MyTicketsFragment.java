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
    private com.google.firebase.firestore.ListenerRegistration listenerRegistration;
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
        
        // Update UI colors for White Theme
        tabPurchased.setTextColor(status.equals("Đã mua") ? 0xFF185FA5 : 0xFF888888);
        tabSold.setTextColor(status.equals("Đã bán") ? 0xFF185FA5 : 0xFF888888);
        tabCancelled.setTextColor(status.equals("Đã hủy") ? 0xFF185FA5 : 0xFF888888);
        
        tabPurchased.setTypeface(null, status.equals("Đã mua") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabSold.setTypeface(null, status.equals("Đã bán") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tabCancelled.setTypeface(null, status.equals("Đã hủy") ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        // Move indicator
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        float tabWidth = screenWidth / 3f;
        float translationX = 0;
        
        if (status.equals("Đã mua")) translationX = 0;
        else if (status.equals("Đã bán")) translationX = tabWidth;
        else if (status.equals("Đã hủy")) translationX = tabWidth * 2;
        
        indicator.animate().translationX(translationX).setDuration(200).start();

        loadTickets();
    }

    private void loadTickets() {
        if (mAuth.getCurrentUser() == null) return;
        String uid = mAuth.getCurrentUser().getUid();

        // 1. Hủy listener cũ nếu đang chạy
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        // 2. Xóa danh sách hiện tại để tránh hiện dữ liệu tab cũ
        ticketList.clear();
        adapter.setTicketList(ticketList);

        // 3. Lắng nghe dữ liệu mới cho tab hiện tại
        listenerRegistration = db.collection("tickets")
                .whereEqualTo("userId", uid)
                .whereEqualTo("status", currentStatus)
                .orderBy("purchaseDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("TICKETS_ERROR", "Lỗi load vé: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        ticketList.clear();
                        java.util.Date now = new java.util.Date();
                        for (QueryDocumentSnapshot doc : value) {
                            Ticket ticket = doc.toObject(Ticket.class);
                            
                            // Lọc bỏ các vé đã kết thúc khỏi mục "Đã mua"
                            if (currentStatus.equals("Đã mua")) {
                                java.util.Date endTime = ticket.getEndTime();
                                if (endTime == null && ticket.getEventDate() != null) {
                                    // Nếu không có giờ kết thúc, mặc định là hết ngày của eventDate
                                    java.util.Calendar cal = java.util.Calendar.getInstance();
                                    cal.setTime(ticket.getEventDate());
                                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                                    cal.set(java.util.Calendar.MINUTE, 59);
                                    endTime = cal.getTime();
                                }
                                
                                if (endTime != null && now.after(endTime)) {
                                    continue; // Bỏ qua vì sự kiện đã kết thúc
                                }
                            }
                            
                            ticketList.add(ticket);
                        }
                        adapter.setTicketList(ticketList);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
