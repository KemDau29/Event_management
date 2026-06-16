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
