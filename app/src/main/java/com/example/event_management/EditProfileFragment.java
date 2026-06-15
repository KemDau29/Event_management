package com.example.event_management;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private EditText edtFullName, edtEmail, edtPhone;
    private TextView tvDob;
    private Spinner spinnerGender;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        uid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        edtFullName = view.findViewById(R.id.edtEditFullName);
        edtEmail = view.findViewById(R.id.edtEditEmail);
        edtPhone = view.findViewById(R.id.edtEditPhone);
        tvDob = view.findViewById(R.id.tvEditDob);
        spinnerGender = view.findViewById(R.id.spinnerGender);

        // Setup Gender Spinner
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        view.findViewById(R.id.btnBackEditProfile).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.layoutPickDate).setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.btnSaveEditProfile).setOnClickListener(v -> saveProfileChanges());

        if (uid != null) {
            loadCurrentUserData();
        }

        return view;
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> tvDob.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1),
                year, month, day);
        datePickerDialog.show();
    }

    private void loadCurrentUserData() {
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                edtFullName.setText(documentSnapshot.getString("fullname"));
                edtEmail.setText(documentSnapshot.getString("email"));
                edtPhone.setText(documentSnapshot.getString("phone"));
                tvDob.setText(documentSnapshot.getString("dob"));
                
                String gender = documentSnapshot.getString("gender");
                if ("Male".equals(gender)) spinnerGender.setSelection(0);
                else if ("Female".equals(gender)) spinnerGender.setSelection(1);
                else spinnerGender.setSelection(2);
            }
        });
    }

    private void saveProfileChanges() {
        String name = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String dob = tvDob.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> userUpdates = new HashMap<>();
        userMapPut(userUpdates, "fullname", name);
        userMapPut(userUpdates, "email", email);
        userMapPut(userUpdates, "phone", phone);
        userMapPut(userUpdates, "dob", dob);
        userMapPut(userUpdates, "gender", gender);

        db.collection("users").document(uid).update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show());
    }

    private void userMapPut(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isEmpty() && !value.equals("Select Date")) {
            map.put(key, value);
        }
    }
}
