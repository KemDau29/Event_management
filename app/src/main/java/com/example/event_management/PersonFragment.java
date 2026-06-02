package com.example.event_management;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PersonFragment extends Fragment {

    private View layoutUserInfo, layoutLoginPrompt;
    private TextView tvPersonName, tvPersonEmail;
    private Button btnGoToLogin;
    private View menuLogout, menuWishlist, menuHistory, menuEditProfile, menuChangePassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View
        layoutUserInfo = view.findViewById(R.id.layoutUserInfo);
        layoutLoginPrompt = view.findViewById(R.id.layoutLoginPrompt);
        tvPersonName = view.findViewById(R.id.tvPersonName);
        tvPersonEmail = view.findViewById(R.id.tvPersonEmail);
        btnGoToLogin = view.findViewById(R.id.btnGoToLogin);
        
        menuWishlist = view.findViewById(R.id.menuWishlist);
        menuHistory = view.findViewById(R.id.menuHistory);
        menuEditProfile = view.findViewById(R.id.menuEditProfile);
        menuChangePassword = view.findViewById(R.id.menuChangePassword);
        menuLogout = view.findViewById(R.id.menuLogout);

        checkUserStatus();

        // Sự kiện click
        btnGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
        });

        menuLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            checkUserStatus();
        });

        menuWishlist.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            } else {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new WishlistFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        menuHistory.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            } else {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HistoryFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            layoutUserInfo.setVisibility(View.VISIBLE);
            layoutLoginPrompt.setVisibility(View.GONE);
            loadUserInfo(currentUser.getUid());
        } else {
            layoutUserInfo.setVisibility(View.GONE);
            layoutLoginPrompt.setVisibility(View.VISIBLE);
        }
    }

    private void loadUserInfo(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("fullname");
                        String email = documentSnapshot.getString("email");
                        tvPersonName.setText(name);
                        tvPersonEmail.setText(email);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkUserStatus(); // Cập nhật lại trạng thái nếu user vừa đăng nhập xong quay lại
    }
}
