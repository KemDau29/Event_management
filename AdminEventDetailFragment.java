package com.example.event_management.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.event_management.R;
import com.example.event_management.models.Comment;
import com.example.event_management.models.Event;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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

    private Event event;
    private FirebaseFirestore db;
    private TextView tvTitle, tvPrice, tvDate, tvLocation, tvDesc, tvRevenue, tvParticipantsCount;
    private ImageView imgBanner, btnBack;
    private LinearLayout layoutParticipants, layoutCommentsList;
    private LineChart chartRevenue;

    public static AdminEventDetailFragment newInstance(Event event) {
        AdminEventDetailFragment fragment = new AdminEventDetailFragment();
        fragment.event = event;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_event_detail, container, false);

        db = FirebaseFirestore.getInstance();

        // Bind Views
        tvTitle = view.findViewById(R.id.tvAdminDetailTitle);
        tvPrice = view.findViewById(R.id.tvAdminDetailPrice);
        tvDate = view.findViewById(R.id.tvAdminDetailDate);
        tvLocation = view.findViewById(R.id.tvAdminDetailLocation);
        tvDesc = view.findViewById(R.id.tvAdminDetailDesc);
        tvRevenue = view.findViewById(R.id.tvAdminDetailRevenue);
        tvParticipantsCount = view.findViewById(R.id.tvAdminDetailParticipantsCount);
        imgBanner = view.findViewById(R.id.imgAdminDetailBanner);
        btnBack = view.findViewById(R.id.btnAdminBackDetail);
        layoutParticipants = view.findViewById(R.id.layoutAdminDetailParticipants);
        layoutCommentsList = view.findViewById(R.id.layoutCommentsList);
        chartRevenue = view.findViewById(R.id.chartRevenue);

        setupChart();

        if (event != null) {
            displayEventDetails();
            loadAnalyticsAndParticipants();
            loadComments();
        }

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void displayEventDetails() {
        tvTitle.setText(event.getTitle());
        tvPrice.setText(String.format(Locale.getDefault(), "%,dđ", event.getPrice()));
        tvDate.setText(String.format("📅 %s", event.getFormattedDate()));
        tvLocation.setText(String.format("📍 %s", event.getLocation()));
        tvDesc.setText(event.getDescription());

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Glide.with(this).load(event.getImageUrl()).into(imgBanner);
        }
    }

    private void setupChart() {
        chartRevenue.getDescription().setEnabled(false);
        chartRevenue.setDrawGridBackground(false);
        chartRevenue.getLegend().setEnabled(false);
        chartRevenue.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chartRevenue.getXAxis().setDrawGridLines(false);
        chartRevenue.getAxisRight().setEnabled(false);
        chartRevenue.getAxisLeft().setDrawGridLines(true);
        chartRevenue.getAxisLeft().setGridColor(Color.LTGRAY);
    }

    private void loadAnalyticsAndParticipants() {
        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            long totalRevenue = 0;
            int totalParticipantsCount = 0;
            Map<String, Integer> userQuantityMap = new HashMap<>();
            TreeMap<String, Long> dailyRevenue = new TreeMap<>(); // Sắp xếp theo ngày

            SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                String userId = doc.getString("userId");
                Date orderDate = doc.getDate("orderDate");

                if (items != null && userId != null) {
                    for (Map<String, Object> item : items) {
                        String eventId = (String) item.get("eventId");
                        if (event.getId().equals(eventId)) {
                            long price = 0;
                            int quantity = 0;

                            Object p = item.get("price");
                            if (p instanceof Long) price = (Long) p;
                            else if (p instanceof Integer) price = (Integer) p;

                            Object q = item.get("quantity");
                            if (q instanceof Long) quantity = ((Long) q).intValue();
                            else if (q instanceof Integer) quantity = (Integer) q;

                            long itemRevenue = price * quantity;
                            totalRevenue += itemRevenue;
                            totalParticipantsCount += quantity;

                            userQuantityMap.put(userId, userQuantityMap.getOrDefault(userId, 0) + quantity);

                            // Cộng dồn doanh thu theo ngày
                            if (orderDate != null) {
                                String day = dayFormat.format(orderDate);
                                dailyRevenue.put(day, dailyRevenue.getOrDefault(day, 0L) + itemRevenue);
                            }
                        }
                    }
                }
            }

            if (isAdded()) {
                tvRevenue.setText(String.format(Locale.getDefault(), "%,dđ", totalRevenue));
                tvParticipantsCount.setText(String.valueOf(totalParticipantsCount));
                updateChart(dailyRevenue);
                renderParticipants(userQuantityMap);
            }
        });
    }

    private void updateChart(TreeMap<String, Long> dailyRevenue) {
        if (dailyRevenue.isEmpty()) {
            chartRevenue.setNoDataText("Chưa có dữ liệu doanh thu");
            chartRevenue.invalidate();
            return;
        }

        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Long> entry : dailyRevenue.entrySet()) {
            entries.add(new Entry(i, entry.getValue().floatValue()));
            labels.add(entry.getKey());
            i++;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.parseColor("#0066FF"));
        dataSet.setCircleColor(Color.parseColor("#0066FF"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(9f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#0066FF"));
        dataSet.setFillAlpha(30);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        chartRevenue.setData(lineData);
        chartRevenue.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartRevenue.getXAxis().setLabelCount(labels.size());
        chartRevenue.invalidate();
    }

    private void renderParticipants(Map<String, Integer> userQuantityMap) {
        layoutParticipants.removeAllViews();
        if (userQuantityMap.isEmpty()) {
            addInfoText(layoutParticipants, "Chưa có người tham gia");
            return;
        }

        for (Map.Entry<String, Integer> entry : userQuantityMap.entrySet()) {
            db.collection("users").document(entry.getKey()).get().addOnSuccessListener(userDoc -> {
                if (!isAdded()) return;
                String name = userDoc.getString("fullname");
                if (name == null) name = userDoc.getString("email");
                if (name == null) name = "Người dùng ẩn";
                addParticipantView(name, entry.getValue());
            });
        }
    }

    private void addParticipantView(String name, int quantity) {
        if (getContext() == null) return;
        TextView textView = new TextView(getContext());
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setPadding(0, 16, 0, 16);
        textView.setText(String.format(Locale.getDefault(), "%s - SL: %d", name, quantity));
        textView.setTextColor(0xFF334155);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        View divider = new View(getContext());
        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
        divider.setBackgroundColor(0xFFF1F5F9);

        layoutParticipants.addView(textView);
        layoutParticipants.addView(divider);
    }

    private void loadComments() {
        db.collection("comments")
                .whereEqualTo("eventId", event.getId())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null && isAdded()) {
                        layoutCommentsList.removeAllViews();
                        for (QueryDocumentSnapshot doc : value) {
                            Comment comment = doc.toObject(Comment.class);
                            addCommentView(comment);
                        }
                    }
                });
    }

    private void addCommentView(Comment comment) {
        View commentView = LayoutInflater.from(getContext()).inflate(R.layout.item_comment, layoutCommentsList, false);
        TextView tvUserName = commentView.findViewById(R.id.tvCommentUserName);
        TextView tvTime = commentView.findViewById(R.id.tvCommentTime);
        TextView tvContent = commentView.findViewById(R.id.tvCommentContent);

        tvUserName.setText(comment.getUserName());
        tvContent.setText(comment.getContent());
        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            tvTime.setText(sdf.format(comment.getTimestamp()));
        }
        layoutCommentsList.addView(commentView);
    }

    private void addInfoText(LinearLayout layout, String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(0, 16, 0, 16);
        textView.setTextColor(Color.GRAY);
        layout.addView(textView);
    }
}
