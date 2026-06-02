package com.example.event_management.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.Login;
import com.example.event_management.R;
import com.example.event_management.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    private TextView tvTotalRevenue, tvTotalTickets, tvTotalUsers;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        tvTotalRevenue = view.findViewById(R.id.tvAdminTotalRevenue);
        tvTotalTickets = view.findViewById(R.id.tvAdminTotalTickets);
        tvTotalUsers = view.findViewById(R.id.tvAdminTotalUsers);

        loadStatistics();

        view.findViewById(R.id.btnAdminLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), Login.class));
            getActivity().finish();
        });

        return view;
    }

    private void loadStatistics() {
        // 1. Thống kê đơn hàng (doanh thu và vé)
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            long revenue = 0;
            int tickets = 0;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                revenue += order.getTotalPrice();
                if (order.getItems() != null) {
                    for (int i = 0; i < order.getItems().size(); i++) {
                        tickets += order.getItems().get(i).getQuantity();
                    }
                }
            }
            tvTotalRevenue.setText(String.format(Locale.getDefault(), "%,dđ", revenue));
            tvTotalTickets.setText(String.valueOf(tickets));
        });

        // 2. Thống kê số lượng người dùng
        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> {
            tvTotalUsers.setText(String.valueOf(queryDocumentSnapshots.size()));
        });
    }
}
