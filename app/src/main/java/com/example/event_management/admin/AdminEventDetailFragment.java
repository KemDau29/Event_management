package com.example.event_management.admin;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class AdminEventDetailFragment extends Fragment {

    private String eventId;
    private String eventTitle;
    private LineChart revenueChart;
    private ListView listParticipants;
    private FirebaseFirestore db;
    private List<String> participants = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_event_detail, container, false);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            eventTitle = getArguments().getString("eventTitle");
        }

        db = FirebaseFirestore.getInstance();
        TextView tvTitle = view.findViewById(R.id.tvAdminEventDetailTitle);
        tvTitle.setText(eventTitle);

        revenueChart = view.findViewById(R.id.revenueChart);
        listParticipants = view.findViewById(R.id.listParticipants);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, participants);
        listParticipants.setAdapter(adapter);

        loadRevenueData();
        loadParticipants();

        return view;
    }

    private void loadRevenueData() {
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            TreeMap<String, Long> revenueByDate = new TreeMap<>(); // Date string -> Revenue
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                if (order.getItems() != null) {
                    for (CartItem item : order.getItems()) {
                        if (item.getEventId().equals(eventId)) {
                            String dateStr = sdf.format(order.getTimestamp() != null ? order.getTimestamp() : new Date());
                            long revenue = (long) item.getPrice() * item.getQuantity();
                            revenueByDate.put(dateStr, revenueByDate.getOrDefault(dateStr, 0L) + revenue);
                        }
                    }
                }
            }

            List<Entry> entries = new ArrayList<>();
            int i = 0;
            final List<String> xLabels = new ArrayList<>();
            for (Map.Entry<String, Long> entry : revenueByDate.entrySet()) {
                entries.add(new Entry(i, entry.getValue()));
                xLabels.add(entry.getKey());
                i++;
            }

            LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);

            LineData lineData = new LineData(dataSet);
            revenueChart.setData(lineData);
            revenueChart.invalidate(); // refresh
        });
    }

    private void loadParticipants() {
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            Map<String, Integer> userTickets = new HashMap<>(); // userId -> quantity
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Order order = doc.toObject(Order.class);
                if (order.getItems() != null) {
                    for (CartItem item : order.getItems()) {
                        if (item.getEventId().equals(eventId)) {
                            String uId = order.getUserId();
                            userTickets.put(uId, userTickets.getOrDefault(uId, 0) + item.getQuantity());
                        }
                    }
                }
            }

            participants.clear();
            if (userTickets.isEmpty()) {
                participants.add("Chưa có người tham gia");
                adapter.notifyDataSetChanged();
            } else {
                for (String uId : userTickets.keySet()) {
                    db.collection("users").document(uId).get().addOnSuccessListener(userDoc -> {
                        String name = userDoc.getString("fullname");
                        if (name == null) name = uId;
                        participants.add(name + " - " + userTickets.get(uId) + " vé");
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });
    }
}
