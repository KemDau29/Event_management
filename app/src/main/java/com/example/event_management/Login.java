package com.example.event_management;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin, btnGoToRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Đổi đúng tên file XML đăng nhập của bạn

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtUsername = findViewById(R.id.edtUsername); // Thay thế đúng ID trong XML của bạn
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);

        btnLogin.setOnClickListener(v -> handleLogin());

        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, register.class));
        });
    }

    private void handleLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tài khoản và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 1: Tìm email tương ứng với Username trong Firestore
        db.collection("users").whereEqualTo("username", username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Lấy document người dùng tìm thấy đầu tiên
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        String email = doc.getString("email");

                        if (email != null) {
                            // Bước 2: Đăng nhập Firebase Auth bằng Email vừa tìm thấy
                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            // Bước 3: Kiểm tra quyền Admin
                                            String role = doc.getString("role");
                                            if (role != null && role.equals("admin")) {
                                                Toast.makeText(Login.this, "Chào mừng Admin!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Login.this, AdminActivity.class));
                                            } else {
                                                Toast.makeText(Login.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Login.this, MainActivity.class));
                                            }
                                            finish();
                                        } else {
                                            Toast.makeText(Login.this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(Login.this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
