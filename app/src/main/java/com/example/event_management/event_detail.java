package com.example.event_management;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Thêm import thư viện Glide
import com.bumptech.glide.Glide;
import com.example.event_management.adapters.CommentAdapter;
import com.example.event_management.models.Comment;
import com.example.event_management.models.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.widget.EditText;
import android.widget.LinearLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class event_detail extends Fragment {

    private static final String ARG_EVENT = "event";
    private Event event;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ImageView btnWishlist;
    private boolean isFavorite = false;

    // Các biến cho Comment
    private LinearLayout layoutCommentsList;
    private EditText edtCommentInput;
    private ImageView btnSendComment;
    private String currentUserName = "Người dùng";

    public event_detail() {
        // Required empty public constructor
    }

    public static event_detail newInstance(Event event) {
        event_detail fragment = new event_detail();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable(ARG_EVENT);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_detail, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            fetchCurrentUserName();
        }

        if (event != null) {
            TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
            TextView tvPrice = view.findViewById(R.id.tvDetailPrice);
            TextView tvDate = view.findViewById(R.id.tvDetailDate);
            TextView tvLocation = view.findViewById(R.id.tvDetailLocation);
            TextView tvDesc = view.findViewById(R.id.tvDetailDesc);

            // Ánh xạ các View cho Comment
            layoutCommentsList = view.findViewById(R.id.layoutCommentsList);
            edtCommentInput = view.findViewById(R.id.edtCommentInput);
            btnSendComment = view.findViewById(R.id.btnSendComment);

            loadComments();

            btnSendComment.setOnClickListener(v -> sendComment());

            // Các View cho field mới
            TextView tvAttendants = view.findViewById(R.id.tvDetailAttendants);
            TextView tvRemaining = view.findViewById(R.id.tvDetailRemaining);
            TextView tvCategory = view.findViewById(R.id.tvDetailCategory);

            ImageView imgBanner = view.findViewById(R.id.imgDetailBanner);
            ImageView btnBack = view.findViewById(R.id.btnBackDetail);
            Button btnAddToCart = view.findViewById(R.id.btnAddToCart);

            tvTitle.setText(event.getTitle());
            tvPrice.setText(String.format(java.util.Locale.getDefault(), "%dđ", event.getPrice()));
            tvDate.setText(String.format("📅 %s", event.getFormattedDate()));
            tvLocation.setText(String.format("📍 %s", event.getLocation()));

            View btnViewMap = view.findViewById(R.id.btnViewMap);
            View.OnClickListener mapClickListener = v -> {
                if (event.getLocation() != null && !event.getLocation().isEmpty()) {
                    String query = event.getLocation();
                    if (event.getLatitude() != 0 && event.getLongitude() != 0) {
                        query = event.getLatitude() + "," + event.getLongitude() + "(" + event.getTitle() + ")";
                    }
                    android.net.Uri gmmIntentUri = android.net.Uri.parse("geo:0,0?q=" + android.net.Uri.encode(query));
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    try {
                        startActivity(mapIntent);
                    } catch (android.content.ActivityNotFoundException e) {
                        android.net.Uri webUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + android.net.Uri.encode(query));
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
                        startActivity(webIntent);
                    }
                } else {
                    Toast.makeText(getContext(), "Không có thông tin địa điểm", Toast.LENGTH_SHORT).show();
                }
            };

            if (btnViewMap != null) btnViewMap.setOnClickListener(mapClickListener);
            tvLocation.setOnClickListener(mapClickListener);

            tvDesc.setText(event.getDescription());

            String eventId = event.getId();

            // TẢI LẠI TOÀN BỘ DỮ LIỆU TỪ FIRESTORE
            db.collection("events").document(event.getId()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot doc = task.getResult();

                            // Kiểm tra xem trường có tồn tại không
                            if (doc.contains("cate")) {
                                DocumentReference cateRef = doc.getDocumentReference("cate");

                                if (cateRef != null) {
                                    cateRef.get().addOnSuccessListener(catDoc -> {
                                        if (catDoc.exists()) {
                                            String catName = catDoc.getString("name");
                                            tvCategory.setText(catName != null ? catName : "Không tên");
                                        } else {
                                            tvCategory.setText("Danh mục không tìm thấy");
                                        }
                                    });
                                } else {
                                    tvCategory.setText("Lỗi Reference");
                                }
                            } else {
                                tvCategory.setText("Không có field 'cate'");
                            }
                        } else {
                            tvCategory.setText("Lỗi kết nối");
                        }
                    });

            if (tvAttendants != null) tvAttendants.setText(String.valueOf(event.getAttendants()));
            if (tvRemaining != null) tvRemaining.setText(String.valueOf(event.getRemainingTickets()));

            // ---- ĐOẠN ĐƯỢC THÊM: Xử lý load ảnh Banner bằng Glide ----
            if (imgBanner != null) {
                if (event.getImageUrl() != null && !event.getImageUrl().trim().isEmpty()) {
                    Glide.with(this)
                            .load(event.getImageUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery) // Ảnh chờ khi đang tải
                            .error(android.R.drawable.ic_menu_gallery)       // Ảnh hiển thị nếu lỗi mạng
                            .into(imgBanner);
                } else {
                    imgBanner.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }
            // ---------------------------------------------------------


            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });

            btnAddToCart.setOnClickListener(v -> addToCart());

            btnWishlist = view.findViewById(R.id.btnWishlist);
            if (btnWishlist != null) {
                checkWishlistStatus();
                btnWishlist.setOnClickListener(v -> toggleWishlist());
            }
        }

        return view;
    }

    private void fetchCurrentUserName() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserName = documentSnapshot.getString("fullname");
                    }
                });
    }

    private void loadComments() {
        if (event == null || event.getId() == null) return;

        db.collection("comments")
                .whereEqualTo("eventId", event.getId())
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("COMMENT_ERROR", "Lỗi load bình luận: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        layoutCommentsList.removeAllViews();
                        for (QueryDocumentSnapshot doc : value) {
                            Comment comment = doc.toObject(Comment.class);
                            addCommentToView(comment);
                        }
                    }
                });
    }

    private void addCommentToView(Comment comment) {
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

    private void sendComment() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = edtCommentInput.getText().toString().trim();
        if (content.isEmpty()) return;

        String uid = mAuth.getCurrentUser().getUid();
        String eventId = event.getId();

        Comment comment = new Comment();
        comment.setEventId(eventId);
        comment.setUserId(uid);
        comment.setUserName(currentUserName);
        comment.setContent(content);
        comment.setTimestamp(new Date());

        db.collection("comments").add(comment)
                .addOnSuccessListener(documentReference -> {
                    edtCommentInput.setText("");
                    Toast.makeText(getContext(), "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi gửi bình luận: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkWishlistStatus() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        db.collection("wishlists").document(uid)
                .collection("items").document(event.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        isFavorite = true;
                        updateWishlistIcon();
                    }
                });
    }

    private void updateWishlistIcon() {
        if (isFavorite) {
            btnWishlist.setImageResource(android.R.drawable.btn_star_big_on);
            btnWishlist.setColorFilter(android.graphics.Color.parseColor("#FFD700")); // Yellow/Gold
        } else {
            btnWishlist.setImageResource(android.R.drawable.btn_star_big_off);
            btnWishlist.setColorFilter(android.graphics.Color.parseColor("#1A1A1A"));
        }
    }

    private void toggleWishlist() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        if (isFavorite) {
            // Remove from wishlist
            db.collection("wishlists").document(uid)
                    .collection("items").document(event.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        updateWishlistIcon();
                        Toast.makeText(getContext(), "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add to wishlist
            db.collection("wishlists").document(uid)
                    .collection("items").document(event.getId())
                    .set(event)
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        updateWishlistIcon();
                        Toast.makeText(getContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void addToCart() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), Login.class);
            startActivity(intent);
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        String eventId = event.getId();

        if (eventId == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID sự kiện", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("eventId", eventId);
        cartItem.put("title", event.getTitle());
        cartItem.put("price", event.getPrice());
        cartItem.put("date", event.getDate());
        cartItem.put("location", event.getLocation());
        cartItem.put("imgUrl", event.getImageUrl());
        cartItem.put("quantity", 1);
        cartItem.put("isChosen", true);

        db.collection("carts").document(uid)
                .collection("cart_items").document(eventId)
                .set(cartItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}