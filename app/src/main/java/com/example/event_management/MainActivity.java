package com.example.event_management;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log; // Thêm import Log để theo dõi kết quả
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.event_management.models.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView imgHome, imgEvents, imgCart, imgProfile, imgChat;
    private TextView tvHome, tvEvents, tvCart, tvProfile, tvChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ==============================================================
        // GỌI HÀM TẠO DATA ĐỂ ĐẨY LÊN FIREBASE (CHỈ CẦN CHẠY 1 LẦN)
        // Sau khi thấy thông báo "Thành công" trên màn hình hoặc Firebase Console,
        // bạn hãy xóa hoặc comment dòng dưới đây lại để tránh tạo trùng lặp data.
        //seedEventsToFirestore();
        // ==============================================================

        // 1. Ánh xạ các vùng Layout nút
        LinearLayout btnHome    = findViewById(R.id.btnHome);
        LinearLayout btnEvents  = findViewById(R.id.btnEvents);
        LinearLayout btnChat    = findViewById(R.id.btnChat);
        LinearLayout btnCart    = findViewById(R.id.btnCart);
        LinearLayout btnProfile = findViewById(R.id.btnProfile);

        // 2. Lấy các View con (Icon và Text) để đổi màu
        imgHome    = (ImageView) btnHome.getChildAt(0);
        tvHome     = (TextView)  btnHome.getChildAt(1);

        imgEvents  = (ImageView) btnEvents.getChildAt(0);
        tvEvents   = (TextView)  btnEvents.getChildAt(1);

        imgChat    = (ImageView) btnChat.getChildAt(0);
        tvChat     = (TextView)  btnChat.getChildAt(1);

        imgCart    = (ImageView) btnCart.getChildAt(0);
        tvCart     = (TextView)  btnCart.getChildAt(1);

        imgProfile = (ImageView) btnProfile.getChildAt(0);
        tvProfile  = (TextView)  btnProfile.getChildAt(1);

        // 3. Thiết lập Fragment mặc định khi mở App
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment(), "home");
        }

        // 4. Sự kiện click cho các nút
        btnHome.setOnClickListener(v -> replaceFragment(new HomeFragment(), "home"));
        btnEvents.setOnClickListener(v -> replaceFragment(new EventFragment(), "events"));
        btnChat.setOnClickListener(v -> replaceFragment(new ChatListFragment(), "chat"));
        btnCart.setOnClickListener(v -> replaceFragment(new CartFragment(), "cart"));
        btnProfile.setOnClickListener(v -> replaceFragment(new PersonFragment(), "profile"));
    }

    /**
     * Hàm tự động đẩy danh sách Event mẫu lên Firestore (Java Version)
     */
    private void seedEventsToFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Map<String, Object>> listEvents = new ArrayList<>();

        // --- CÔNG NGHỆ (Cong-nghe) ---
        Map<String, Object> e1 = new HashMap<>();
        e1.put("title", "Workshop Flutter Nâng Cao");
        e1.put("attendants", 0);
        e1.put("location", "Phòng B2 - Khu Công Nghệ");
        e1.put("price", 20000);
        e1.put("remainingTickets", 20);
        e1.put("description", "Tiếp tục chuỗi workshop thực chiến nâng cao cùng chuyên gia Google Developer Expert.");
        e1.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e1.put("date", new Date());
        e1.put("cate", db.document("/categories/Cong-nghe"));
        listEvents.add(e1);

        Map<String, Object> e2 = new HashMap<>();
        e2.put("title", "Sự kiện Tech Talk 2026");
        e2.put("attendants", 5);
        e2.put("location", "Hội trường A - Đại học CNTT");
        e2.put("price", 0);
        e2.put("remainingTickets", 150);
        e2.put("description", "Cập nhật và thảo luận về các xu hướng Trí tuệ nhân tạo (AI) và Big Data trong năm mới.");
        e2.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e2.put("date", new Date());
        e2.put("cate", db.document("/categories/Cong-nghe"));
        listEvents.add(e2);

        Map<String, Object> e3 = new HashMap<>();
        e3.put("title", "Hackathon: Lập Trình Xanh");
        e3.put("attendants", 12);
        e3.put("location", "Trung tâm Đổi mới Sáng tạo");
        e3.put("price", 50000);
        e3.put("remainingTickets", 30);
        e3.put("description", "Cuộc thi lập trình 48 giờ liên tục giải quyết các bài toán môi trường toàn cầu.");
        e3.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e3.put("date", new Date());
        e3.put("cate", db.document("/categories/Cong-nghe"));
        listEvents.add(e3);

        // --- ÂM NHẠC (Am-nhac) ---
        Map<String, Object> e4 = new HashMap<>();
        e4.put("title", "Đêm Nhạc Acoustic: Lời Yêu Thương");
        e4.put("attendants", 45);
        e4.put("location", "Trà Chanh Phố Cổ Cafe");
        e4.put("price", 80000);
        e4.put("remainingTickets", 15);
        e4.put("description", "Không gian âm nhạc nhẹ nhàng, lắng đọng với những bản tình ca bất hủ ca khúc ballad.");
        e4.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e4.put("date", new Date());
        e4.put("cate", db.document("/categories/Am-nhac"));
        listEvents.add(e4);

        Map<String, Object> e5 = new HashMap<>();
        e5.put("title", "Hòa Nhạc Thính Phòng Classic");
        e5.put("attendants", 110);
        e5.put("location", "Nhà Hát Thành Phố");
        e5.put("price", 350000);
        e5.put("remainingTickets", 40);
        e5.put("description", "Đêm trình diễn nhạc cổ điển giao hưởng đỉnh cao từ các nghệ sĩ quốc tế nổi tiếng.");
        e5.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e5.put("date", new Date());
        e5.put("cate", db.document("/categories/Am-nhac"));
        listEvents.add(e5);

        // --- THỂ THAO (The-thao) ---
        Map<String, Object> e6 = new HashMap<>();
        e6.put("title", "Giải Chạy Marathon Vì Cộng Đồng");
        e6.put("attendants", 500);
        e6.put("location", "Công viên Bờ Sông Quận 2");
        e6.put("price", 150000);
        e6.put("remainingTickets", 200);
        e6.put("description", "Giải chạy cự ly 5km và 10km nhằm gây quỹ xây dựng trường học cho trẻ em vùng cao.");
        e6.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e6.put("date", new Date());
        e6.put("cate", db.document("/categories/The-thao"));
        listEvents.add(e6);

        Map<String, Object> e7 = new HashMap<>();
        e7.put("title", "Giải Đua Xe Đạp Mở Rộng 2026");
        e7.put("attendants", 20);
        e7.put("location", "Cung đường Mai Chí Thọ");
        e7.put("price", 100000);
        e7.put("remainingTickets", 80);
        e7.put("description", "Cuộc tranh tài kịch tính của các cua-rơ bán chuyên nghiệp toàn quốc.");
        e7.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e7.put("date", new Date());
        e7.put("cate", db.document("/categories/The-thao"));
        listEvents.add(e7);

        // --- TRIỂN LÃM (Trien-lam) ---
        Map<String, Object> e8 = new HashMap<>();
        e8.put("title", "Triển Lãm Tranh: Nét Thời Gian");
        e8.put("attendants", 15);
        e8.put("location", "Bảo tàng Mỹ thuật thành phố");
        e8.put("price", 30000);
        e8.put("remainingTickets", 100);
        e8.put("description", "Trưng bày hơn 50 tác phẩm tranh sơn dầu khắc họa cuộc sống Sài Gòn xưa và nay.");
        e8.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e8.put("date", new Date());
        e8.put("cate", db.document("/categories/Trien-lam"));
        listEvents.add(e8);

        Map<String, Object> e9 = new HashMap<>();
        e9.put("title", "Expo Công Nghệ Đồ Gia Dụng Smart");
        e9.put("attendants", 240);
        e9.put("location", "Trung tâm triển lãm SECC");
        e9.put("price", 0);
        e9.put("remainingTickets", 1000);
        e9.put("description", "Nơi quy tụ các thương hiệu smarthome và thiết bị gia dụng thông minh thế hệ mới.");
        e9.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e9.put("date", new Date());
        e9.put("cate", db.document("/categories/Trien-lam"));
        listEvents.add(e9);

        // --- GAMESHOW (Gameshow) ---
        Map<String, Object> e10 = new HashMap<>();
        e10.put("title", "Gameshow: Siêu Trí Tuệ Học Đường");
        e10.put("attendants", 80);
        e10.put("location", "Hội trường Lớn Khu trung tâm");
        e10.put("price", 10000);
        e10.put("remainingTickets", 120);
        e10.put("description", "Đấu trường trí tuệ căng thẳng giữa các tài năng toán học và tư duy logic đỉnh cao.");
        e10.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e10.put("date", new Date());
        e10.put("cate", db.document("/categories/Gameshow"));
        listEvents.add(e10);

        Map<String, Object> e11 = new HashMap<>();
        e11.put("title", "Đêm Hội Âm Nhạc & Trò Chơi Nhanh Như Chớp");
        e11.put("attendants", 34);
        e11.put("location", "Sân Khấu Kịch Hồng Vân");
        e11.put("price", 50000);
        e11.put("remainingTickets", 46);
        e11.put("description", "Gameshow tương tác trực tiếp giải đố mẹo, mang lại tiếng cười sảng khoái và phần quà hấp dẫn.");
        e11.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e11.put("date", new Date());
        e11.put("cate", db.document("/categories/Gameshow"));
        listEvents.add(e11);

        // --- NGHỆ THUẬT (Nghe-thuat) ---
        Map<String, Object> e12 = new HashMap<>();
        e12.put("title", "Kịch Nói: Đêm Lạnh Chùa Hoang");
        e12.put("attendants", 95);
        e12.put("location", "Sân khấu Kịch IDECAF");
        e12.put("price", 180000);
        e12.put("remainingTickets", 25);
        e12.put("description", "Vở diễn nghệ thuật cải lương kịch nói kinh điển được dàn dựng lại bởi đạo diễn gạo cội.");
        e12.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e12.put("date", new Date());
        e12.put("cate", db.document("/categories/Nghe-thuat"));
        listEvents.add(e12);

        Map<String, Object> e13 = new HashMap<>();
        e13.put("title", "Lớp Học Làm Gốm Thủ Công");
        e13.put("attendants", 8);
        e13.put("location", "Vườn Gốm Xoài Studio");
        e13.put("price", 250000);
        e13.put("remainingTickets", 12);
        e13.put("description", "Trải nghiệm tự tay làm ra sản phẩm gốm độc bản mang bản sắc cá nhân dưới sự hướng dẫn tỉ mỉ.");
        e13.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e13.put("date", new Date());
        e13.put("cate", db.document("/categories/Nghe-thuat"));
        listEvents.add(e13);

        // --- BỔ SUNG THÊM ĐỂ ĐỦ 15 SỰ KIỆN ---
        Map<String, Object> e14 = new HashMap<>();
        e14.put("title", "Giải Đấu Esport: King Of RAG");
        e14.put("attendants", 64);
        e14.put("location", "Vikings Gaming Center");
        e14.put("price", 20000);
        e14.put("remainingTickets", 100);
        e14.put("description", "Giải đấu thể thao điện tử dành cho sinh viên giao lưu tranh cúp vô địch.");
        e14.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e14.put("date", new Date());
        e14.put("cate", db.document("/categories/Gameshow"));
        listEvents.add(e14);

        Map<String, Object> e15 = new HashMap<>();
        e15.put("title", "Hội Chợ Sách Cũ & Nghệ Thuật Sách");
        e15.put("attendants", 120);
        e15.put("location", "Đường Sách Nguyễn Văn Bình");
        e15.put("price", 0);
        e15.put("remainingTickets", 500);
        e15.put("description", "Giao lưu, trao đổi sách cổ quý hiếm và tham gia workshop đóng bìa sách nghệ thuật.");
        e15.put("imageUrl", "https://i.imgur.com/BOPaQQ5.png");
        e15.put("date", new Date());
        e15.put("cate", db.document("/categories/Nghe-thuat"));
        listEvents.add(e15);

        // 3. Sử dụng WriteBatch đẩy đồng loạt 15 bản ghi lên Firestore
        WriteBatch batch = db.batch();
        for (Map<String, Object> event : listEvents) {
            DocumentReference newDocRef = db.collection("events").document();
            batch.set(newDocRef, event);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore_Seed", "Đã thêm thành công 15 sự kiện mẫu!");
                    Toast.makeText(MainActivity.this, "Đã khởi tạo xong 15 sự kiện mẫu!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore_Seed", "Lỗi sinh data: ", e);
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

        imgEvents.setColorFilter(activeTab.equals("events") ? active : inactive);
        tvEvents.setTextColor(activeTab.equals("events") ? active : inactive);

        imgChat.setColorFilter(activeTab.equals("chat") ? active : inactive);
        tvChat.setTextColor(activeTab.equals("chat") ? active : inactive);

        imgCart.setColorFilter(activeTab.equals("cart") ? active : inactive);
        tvCart.setTextColor(activeTab.equals("cart") ? active : inactive);

        imgProfile.setColorFilter(activeTab.equals("profile") ? active : inactive);
        tvProfile.setTextColor(activeTab.equals("profile") ? active : inactive);
    }
}