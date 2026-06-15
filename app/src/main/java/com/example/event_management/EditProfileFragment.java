package com.example.event_management;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";
    private static final int MAX_IMAGE_SIZE = 500 * 1024; // 500KB max
    private EditText edtFullName, edtEmail, edtPhone;
    private TextView tvDob;
    private ImageView imgAvatar;
    private Spinner spinnerGender;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String uid;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgAvatar.setImageURI(uri);
                }
            }
    );

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && selectedImageUri != null) {
                    imgAvatar.setImageURI(selectedImageUri);
                }
            }
    );

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            results -> {
                Boolean cameraGranted = results.get(Manifest.permission.CAMERA);
                Boolean storageGranted = results.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                
                if (cameraGranted != null && cameraGranted) {
                    openCamera();
                }
            }
    );

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
        imgAvatar = view.findViewById(R.id.imgEditAvatar);
        spinnerGender = view.findViewById(R.id.spinnerGender);

        // Setup Gender Spinner
        String[] genders = {"Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        view.findViewById(R.id.btnBackEditProfile).setOnClickListener(v -> getParentFragmentManager().popBackStack());
        view.findViewById(R.id.layoutPickDate).setOnClickListener(v -> showDatePicker());
        view.findViewById(R.id.btnPickImage).setOnClickListener(v -> showImagePickerDialog());
        view.findViewById(R.id.btnSaveEditProfile).setOnClickListener(v -> saveProfileChanges());

        if (uid != null) {
            loadCurrentUserData();
        }

        return view;
    }

    private void showImagePickerDialog() {
        CharSequence[] options = {"Chụp ảnh", "Chọn từ thư viện", "Hủy"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn ảnh")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Log.d(TAG, "User selected camera");
                        checkCameraPermissionAndOpen();
                    } else if (which == 1) {
                        Log.d(TAG, "User selected gallery");
                        pickImageLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void checkCameraPermissionAndOpen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionsLauncher.launch(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                });
            } else {
                openCamera();
            }
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        try {
            // Create file in app's files directory (more reliable than cache)
            File photoFile = new File(requireContext().getFilesDir(), "photo_" + System.currentTimeMillis() + ".jpg");
            
            // Ensure parent directory exists
            if (!photoFile.getParentFile().exists()) {
                photoFile.getParentFile().mkdirs();
            }
            
            selectedImageUri = FileProvider.getUriForFile(requireContext(), 
                    requireContext().getPackageName() + ".fileprovider", photoFile);
            
            Log.d(TAG, "Camera URI created: " + selectedImageUri);
            takePictureLauncher.launch(selectedImageUri);
        } catch (Exception e) {
            Log.e(TAG, "Failed to open camera", e);
            Toast.makeText(requireContext(), "Không thể mở camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
                
                String avatarBase64 = documentSnapshot.getString("avatarUrl");
                Log.d(TAG, "avatarUrl value: " + (avatarBase64 != null ? "exists, length=" + avatarBase64.length() : "null"));
                
                if (avatarBase64 != null && !avatarBase64.isEmpty() && !avatarBase64.equals("null")) {
                    Log.d(TAG, "Loading avatar from Base64");
                    try {
                        byte[] decodedString = Base64.decode(avatarBase64, Base64.DEFAULT);
                        Log.d(TAG, "Decoded bytes length: " + decodedString.length);
                        
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        if (decodedBitmap != null) {
                            Log.d(TAG, "Bitmap created successfully");
                            imgAvatar.setImageBitmap(decodedBitmap);
                        } else {
                            Log.e(TAG, "Bitmap is null after decoding");
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "IllegalArgumentException: Failed to decode Base64 image", e);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception: Failed to decode Base64 image", e);
                    }
                } else {
                    Log.d(TAG, "No avatar URL found or empty");
                }

                String gender = documentSnapshot.getString("gender");
                if ("Male".equals(gender)) spinnerGender.setSelection(0);
                else if ("Female".equals(gender)) spinnerGender.setSelection(1);
                else spinnerGender.setSelection(2);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load user data", e);
            Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileChanges() {
        String name = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String dob = tvDob.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri != null) {
            uploadImageAndSaveProfile(name, email, phone, dob, gender);
        } else {
            updateProfileFirestore(name, email, phone, dob, gender, null);
        }
    }

    private void uploadImageAndSaveProfile(String name, String email, String phone, String dob, String gender) {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "Chưa chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Đang xử lý ảnh");
        progressDialog.setMessage("Vui lòng chờ...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Log.d(TAG, "Starting image conversion to Base64. URI: " + selectedImageUri);

        // Convert image to Base64 on background thread
        new Thread(() -> {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
                
                if (inputStream == null) {
                    throw new Exception("Không thể mở file ảnh");
                }
                
                Log.d(TAG, "InputStream opened successfully");
                
                // Compress bitmap to reduce size
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                
                if (bitmap == null) {
                    throw new Exception("Không thể decode ảnh");
                }
                
                Log.d(TAG, "Bitmap decoded. Original size: " + bitmap.getByteCount() / 1024 + " KB");
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                
                // Compress to quality that keeps size under 500KB
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                
                // If too large, reduce quality
                while (outputStream.toByteArray().length > MAX_IMAGE_SIZE && quality > 20) {
                    quality -= 10;
                    outputStream.reset();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                }
                
                Log.d(TAG, "Bitmap compressed to quality: " + quality + ", size: " + (outputStream.toByteArray().length / 1024) + " KB");
                
                String base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
                Log.d(TAG, "Image converted to Base64. Encoded size: " + (base64Image.length() / 1024) + " KB");
                
                outputStream.close();
                bitmap.recycle();
                
                // Save to Firestore on main thread
                requireActivity().runOnUiThread(() -> {
                    updateProfileFirestore(name, email, phone, dob, gender, base64Image);
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Failed to convert image", e);
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(getContext(), "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void updateProfileFirestore(String name, String email, String phone, String dob, String gender, String avatarBase64) {
        Map<String, Object> userUpdates = new HashMap<>();
        userMapPut(userUpdates, "fullname", name);
        userMapPut(userUpdates, "email", email);
        userMapPut(userUpdates, "phone", phone);
        userMapPut(userUpdates, "dob", dob);
        userMapPut(userUpdates, "gender", gender);
        
        if (avatarBase64 != null && !avatarBase64.isEmpty()) {
            Log.d(TAG, "Adding avatar to update. Base64 length: " + avatarBase64.length());
            userUpdates.put("avatarUrl", avatarBase64);
        } else {
            Log.d(TAG, "No avatar to update");
        }

        Log.d(TAG, "Updating profile with fields: " + userUpdates.keySet().toString());

        db.collection("users").document(uid).update(userUpdates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Profile updated successfully");
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(getContext(), "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update profile", e);
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(getContext(), "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void userMapPut(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isEmpty() && !value.equals("Select Date")) {
            map.put(key, value);
        }
    }
}
