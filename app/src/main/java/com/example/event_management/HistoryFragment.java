package com.example.event_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.adapters.HistoryAdapter;
import com.example.event_management.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private ListView listHistory;
    private HistoryAdapter adapter;
    private List<Order> orderList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        listHistory = view.findViewById(R.id.listHistory);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        
        orderList = new ArrayList<>();
        adapter = new HistoryAdapter(requireContext());
        listHistory.setAdapter(adapter);

        if (mAuth.getCurrentUser() != null) {
            loadHistory();
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem lịch sử", Toast.LENGTH_SHORT).show();
        }

        view.findViewById(R.id.btnBackHistory).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void loadHistory() {
        if (mAuth.getCurrentUser() == null) {
            android.util.Log.d("HISTORY_DEBUG", "User chưa đăng nhập");
            return;
        }
        
        String uid = mAuth.getCurrentUser().getUid();
        android.util.Log.d("HISTORY_DEBUG", "Đang load lịch sử cho UID: " + uid);
        
        db.collection("orders")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("HISTORY_DEBUG", "Lỗi query: " + error.getMessage());
                        // Nếu lỗi là FAILED_PRECONDITION, Firestore sẽ cung cấp link tạo index trong logcat hệ thống
                        //Toast.makeText(getContext(), "Lỗi tải dữ liệu. Kiểm tra Logcat để tạo Index nếu cần.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (value != null) {
                        android.util.Log.d("HISTORY_DEBUG", "Số lượng đơn hàng tìm thấy: " + value.size());
                        orderList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Order order = doc.toObject(Order.class);
                                orderList.add(order);
                                android.util.Log.d("HISTORY_DEBUG", "Đã load đơn: " + order.getOrderId());
                            } catch (Exception e) {
                                android.util.Log.e("HISTORY_DEBUG", "Lỗi convert document: " + e.getMessage());
                            }
                        }
                        adapter.setOrderList(orderList);
                    } else {
                        android.util.Log.d("HISTORY_DEBUG", "Value là null");
                    }
                });
    }
}
