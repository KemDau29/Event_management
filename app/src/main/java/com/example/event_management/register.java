package com.example.event_management;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {

    private EditText edtFullName, edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Đổi đúng tên file XML đăng ký của bạn

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtFullName = findViewById(R.id.edtFullName); // Thay thế đúng ID trong XML của bạn
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> handleRegister());
    }

    private void handleRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 1: Kiểm tra xem username đã tồn tại trong Firestore chưa
                db.collection("users").whereEqualTo("username", username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(register.this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Bước 2: Tạo tài khoản trên Firebase Authentication
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(authTask -> {
                                    if (authTask.isSuccessful()) {
                                        String uid = mAuth.getCurrentUser().getUid();

                                        // Bước 3: Lưu thông tin User vào Firestore
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("uid", uid);
                                        userMap.put("fullname", fullName);
                                        userMap.put("username", username);
                                        userMap.put("email", email);
                                        userMap.put("password", password); // Thực tế không nên lưu mật khẩu dạng text thô

                                        db.collection("users").document(uid).set(userMap)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(register.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                                    finish(); // Quay lại màn hình Đăng nhập
                                                })
                                                .addOnFailureListener(e -> Toast.makeText(register.this, "Lưu thông tin thất bại", Toast.LENGTH_SHORT).show());
                                    } else {
                                        Toast.makeText(register.this, "Đăng ký thất bại: " + authTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }
}