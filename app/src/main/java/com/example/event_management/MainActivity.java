package com.example.event_management;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView imgHome, imgTickets, imgCart, imgProfile, imgChat;
    private TextView tvHome, tvCart, tvProfile, tvChat;
    private MaterialCardView btnTickets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Làm trắng thanh điều hướng hệ thống (nếu có)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().setNavigationBarColor(Color.WHITE);
        }

        setContentView(R.layout.activity_main);

        // 1. Ánh xạ các vùng Layout nút
        LinearLayout btnHome          = findViewById(R.id.btnHome);
        LinearLayout btnChat          = findViewById(R.id.btnChat);
        btnTickets                    = findViewById(R.id.btnMyTicketsNav);
        LinearLayout btnCart          = findViewById(R.id.btnCart);
        LinearLayout btnProfile       = findViewById(R.id.btnProfile);

        // 2. Lấy các View con (Icon và Text) để đổi màu
        imgHome          = (ImageView) btnHome.getChildAt(0);
        tvHome           = (TextView)  btnHome.getChildAt(1);

        imgChat          = (ImageView) btnChat.getChildAt(0);
        tvChat           = (TextView)  btnChat.getChildAt(1);

        imgTickets       = (ImageView) btnTickets.getChildAt(0);

        imgCart          = (ImageView) btnCart.getChildAt(0);
        tvCart           = (TextView)  btnCart.getChildAt(1);

        imgProfile       = (ImageView) btnProfile.getChildAt(0);
        tvProfile        = (TextView)  btnProfile.getChildAt(1);

        // 3. Thiết lập Fragment mặc định khi mở App
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment(), "home");
        }

        // 4. Sự kiện click cho các nút
        btnHome.setOnClickListener(v -> replaceFragment(new HomeFragment(), "home"));
        btnChat.setOnClickListener(v -> replaceFragment(new ChatListFragment(), "chat"));
        btnTickets.setOnClickListener(v -> replaceFragment(new MyTicketsFragment(), "tickets"));
        btnCart.setOnClickListener(v -> replaceFragment(new CartFragment(), "cart"));
        btnProfile.setOnClickListener(v -> replaceFragment(new PersonFragment(), "profile"));

        // Cập nhật 6 sự kiện sang tháng 7/2026 (Chạy 1 lần rồi comment lại)
        // updateEventsToJuly2026();
        
        // Tạo 5 tổ chức mẫu nếu chưa có
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("organizations").limit(1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                seedOrganizations();
            }
        });
    }

    private void seedOrganizations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Map<String, Object>> orgs = new ArrayList<>();

        Map<String, Object> o1 = new HashMap<>();
        o1.put("name", "FPT Software");
        o1.put("description", "Tập đoàn công nghệ hàng đầu Việt Nam.");
        o1.put("logoUrl", "https://i.imgur.com/BOPaQQ5.png");
        o1.put("followers", new ArrayList<>());
        orgs.add(o1);

        Map<String, Object> o2 = new HashMap<>();
        o2.put("name", "VNG Corporation");
        o2.put("description", "Công ty internet và trò chơi trực tuyến hàng đầu.");
        o2.put("logoUrl", "https://i.imgur.com/BOPaQQ5.png");
        o2.put("followers", new ArrayList<>());
        orgs.add(o2);

        Map<String, Object> o3 = new HashMap<>();
        o3.put("name", "Viettel Group");
        o3.put("description", "Tập đoàn viễn thông quân đội Việt Nam.");
        o3.put("logoUrl", "https://i.imgur.com/BOPaQQ5.png");
        o3.put("followers", new ArrayList<>());
        orgs.add(o3);

        Map<String, Object> o4 = new HashMap<>();
        o4.put("name", "Google Developers Group");
        o4.put("description", "Cộng đồng lập trình viên Google.");
        o4.put("logoUrl", "https://i.imgur.com/BOPaQQ5.png");
        o4.put("followers", new ArrayList<>());
        orgs.add(o4);

        Map<String, Object> o5 = new HashMap<>();
        o5.put("name", "TEDx Vietnam");
        o5.put("description", "Diễn đàn chia sẻ ý tưởng lan tỏa.");
        o5.put("logoUrl", "https://i.imgur.com/BOPaQQ5.png");
        o5.put("followers", new ArrayList<>());
        orgs.add(o5);

        WriteBatch batch = db.batch();
        List<DocumentReference> orgRefs = new ArrayList<>();

        for (Map<String, Object> org : orgs) {
            DocumentReference ref = db.collection("organizations").document();
            batch.set(ref, org);
            orgRefs.add(ref);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            // Sau khi tạo xong Org, gán ngẫu nhiên sự kiện hiện có cho các Org này
            db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
                WriteBatch eventBatch = db.batch();
                int i = 0;
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    DocumentReference randomOrg = orgRefs.get(i % orgRefs.size());
                    eventBatch.update(doc.getReference(), "organizerId", randomOrg.getId());
                    i++;
                }
                eventBatch.commit().addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đã khởi tạo 5 đơn vị tổ chức thành công!", Toast.LENGTH_SHORT).show();
                });
            });
        });
    }

    private void updateEventsToJuly2026() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").limit(6).get().addOnSuccessListener(queryDocumentSnapshots -> {
            WriteBatch batch = db.batch();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(2026, java.util.Calendar.JULY, 1, 8, 0); // Bắt đầu từ 01/07/2026

            int dayOffset = 5; 
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                cal.add(java.util.Calendar.DAY_OF_MONTH, dayOffset);
                Date eventDate = cal.getTime();
                
                // Hạn đăng ký: 2 ngày trước sự kiện
                Date closeDate = new Date(eventDate.getTime() - (2L * 24 * 60 * 60 * 1000));
                // Ngày mở: 7 ngày trước ngày đóng
                Date openDate = new Date(closeDate.getTime() - (7L * 24 * 60 * 60 * 1000));

                Map<String, Object> updates = new HashMap<>();
                updates.put("date", eventDate);
                updates.put("ticketOpenDate", openDate);
                updates.put("ticketCloseDate", closeDate);
                
                batch.update(doc.getReference(), updates);
                dayOffset += 3;
            }

            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Đã cập nhật 6 sự kiện sang tháng 7/2026", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void replaceFragment(Fragment fragment, String tabTag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        updateNavColors(tabTag);
    }

    private void updateNavColors(String activeTab) {
        int active   = Color.parseColor("#185FA5");
        int inactive = Color.parseColor("#AAAAAA");

        imgHome.setColorFilter(activeTab.equals("home") ? active : inactive);
        tvHome.setTextColor(activeTab.equals("home") ? active : inactive);

        imgChat.setColorFilter(activeTab.equals("chat") ? active : inactive);
        tvChat.setTextColor(activeTab.equals("chat") ? active : inactive);

        // Nút trung tâm đổi màu nền Card thay vì màu icon nếu muốn, hoặc đổi màu icon trắng
        btnTickets.setCardBackgroundColor(activeTab.equals("tickets") ? active : Color.parseColor("#F5F6F8"));
        imgTickets.setColorFilter(activeTab.equals("tickets") ? Color.WHITE : inactive);

        imgCart.setColorFilter(activeTab.equals("cart") ? active : inactive);
        tvCart.setTextColor(activeTab.equals("cart") ? active : inactive);

        imgProfile.setColorFilter(activeTab.equals("profile") ? active : inactive);
        tvProfile.setTextColor(activeTab.equals("profile") ? active : inactive);
    }
}
