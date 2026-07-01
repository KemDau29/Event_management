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
import com.example.event_management.adapters.TicketTypeAdapter;
import com.example.event_management.adapters.TimelineAdapter;
import com.example.event_management.models.Comment;
import com.example.event_management.models.Event;
import com.example.event_management.models.Organization;
import com.example.event_management.models.TicketType;
import com.example.event_management.models.TimelineItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.event_management.adapters.ShareEventAdapter;
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
    private RecyclerView rvTicketTypes;
    private TicketTypeAdapter ticketTypeAdapter;
    private List<TicketType> ticketTypesList = new ArrayList<>();
    private RecyclerView rvTimeline;
    private TimelineAdapter timelineAdapter;
    private List<TimelineItem> timelineList = new ArrayList<>();

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
            TextView tvTimeRange = view.findViewById(R.id.tvDetailTimeRange);
            TextView tvDeadline = view.findViewById(R.id.tvRegistrationDeadline);
            
            View cardTicketOpen = view.findViewById(R.id.cardTicketOpenInfo);
            TextView tvRegPeriod = view.findViewById(R.id.tvDetailRegistrationPeriod);
            TextView tvActualDate = view.findViewById(R.id.tvDetailEventActualDate);
            TextView tvAnnounce = view.findViewById(R.id.tvDetailAnnouncementDate);

            // Các View hiển thị vé sớm còn lại
            View layoutEBRemaining = view.findViewById(R.id.layoutEarlyBirdRemaining);
            TextView tvEBRemaining = view.findViewById(R.id.tvDetailEarlyBirdRemaining);

            TextView tvOrgName = view.findViewById(R.id.tvDetailOrgName);
            ImageView imgOrgLogo = view.findViewById(R.id.imgDetailOrgLogo);
            View layoutOrg = view.findViewById(R.id.layoutOrganizer);

            // Ánh xạ các View cho Comment
            layoutCommentsList = view.findViewById(R.id.layoutCommentsList);
            edtCommentInput = view.findViewById(R.id.edtCommentInput);
            btnSendComment = view.findViewById(R.id.btnSendComment);

            loadComments();

            btnSendComment.setOnClickListener(v -> sendComment());

            // Ticket Types RecyclerView
            rvTicketTypes = view.findViewById(R.id.rvTicketTypes);
            rvTicketTypes.setLayoutManager(new LinearLayoutManager(getContext()));
            ticketTypeAdapter = new TicketTypeAdapter(ticketTypesList);
            rvTicketTypes.setAdapter(ticketTypeAdapter);

            rvTimeline = view.findViewById(R.id.rvTimeline);
            rvTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
            timelineAdapter = new TimelineAdapter(timelineList);
            rvTimeline.setAdapter(timelineAdapter);

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

                            // Load Ticket Types
                            ticketTypesList.clear();
                            List<HashMap<String, Object>> types = (List<HashMap<String, Object>>) doc.get("ticketTypes");
                            if (types != null && !types.isEmpty()) {
                                for (HashMap<String, Object> t : types) {
                                    TicketType tt = new TicketType();
                                    tt.setName((String) t.get("name"));
                                    tt.setPrice(((Long) t.get("price")).intValue());
                                    tt.setDescription((String) t.get("description"));
                                    
                                    // Load Early Bird & Limit fields
                                    if (t.containsKey("maxQuantity")) {
                                        tt.setMaxQuantity(((Long) t.get("maxQuantity")).intValue());
                                    }
                                    if (t.containsKey("soldQuantity")) {
                                        tt.setSoldQuantity(((Long) t.get("soldQuantity")).intValue());
                                    }
                                    if (t.containsKey("isEarlyBird")) {
                                        tt.setEarlyBird((Boolean) t.get("isEarlyBird"));
                                    }
                                    if (t.containsKey("deadline")) {
                                        Object deadlineObj = t.get("deadline");
                                        if (deadlineObj instanceof com.google.firebase.Timestamp) {
                                            tt.setDeadline(((com.google.firebase.Timestamp) deadlineObj).toDate());
                                        } else if (deadlineObj instanceof java.util.Date) {
                                            tt.setDeadline((java.util.Date) deadlineObj);
                                        }
                                    }

                                    ticketTypesList.add(tt);
                                }
                            } else {
                                // Default types if none exist
                                ticketTypesList.add(new TicketType("Phổ thông", event.getPrice(), "Vé tham gia cơ bản"));
                                ticketTypesList.add(new TicketType("VIP", event.getPrice() * 2, "Vị trí ưu tiên, quà tặng kèm"));
                            }
                            ticketTypeAdapter.notifyDataSetChanged();

                            // Load Timeline
                            timelineList.clear();
                            java.text.SimpleDateFormat sdfTime = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                            
                            if (doc.contains("startTime") && doc.contains("endTime")) {
                                java.util.Date start = doc.getDate("startTime");
                                java.util.Date end = doc.getDate("endTime");
                                if (start != null && end != null) {
                                    tvTimeRange.setText("🕒 " + sdfTime.format(start) + " - " + sdfTime.format(end));
                                    tvTimeRange.setVisibility(View.VISIBLE);
                                }
                            }

                            List<HashMap<String, Object>> timelineData = (List<HashMap<String, Object>>) doc.get("timeline");
                            if (timelineData != null && !timelineData.isEmpty()) {
                                for (HashMap<String, Object> item : timelineData) {
                                    TimelineItem ti = new TimelineItem();
                                    
                                    Object startObj = item.get("startTime");
                                    if (startObj instanceof com.google.firebase.Timestamp) {
                                        ti.setStartTime(((com.google.firebase.Timestamp) startObj).toDate());
                                    } else if (startObj instanceof java.util.Date) {
                                        ti.setStartTime((java.util.Date) startObj);
                                    }

                                    Object endObj = item.get("endTime");
                                    if (endObj instanceof com.google.firebase.Timestamp) {
                                        ti.setEndTime(((com.google.firebase.Timestamp) endObj).toDate());
                                    } else if (endObj instanceof java.util.Date) {
                                        ti.setEndTime((java.util.Date) endObj);
                                    }

                                    ti.setActivity((String) item.get("activity"));
                                    timelineList.add(ti);
                                }
                            } else {
                                // Default timeline logic with current date if needed
                                // (Bạn có thể bỏ phần này nếu database của bạn đã chuẩn)
                            }
                            timelineAdapter.notifyDataSetChanged();

                            // Load Registration Period & Announcement
                            Locale localeVN = new Locale("vi", "VN");
                            SimpleDateFormat sdfPretty = new SimpleDateFormat("dd 'tháng' MM, yyyy", localeVN);
                            SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm", localeVN);
                            
                            Date openDate = null;
                            Date closeDate = null;
                            Date ebOpen = null;
                            Date ebClose = null;

                            if (doc.contains("ticketOpenDate")) {
                                openDate = doc.getDate("ticketOpenDate");
                            }
                            if (doc.contains("ticketCloseDate")) {
                                closeDate = doc.getDate("ticketCloseDate");
                            }
                            if (doc.contains("earlyBirdOpenDate")) {
                                ebOpen = doc.getDate("earlyBirdOpenDate");
                                event.setEarlyBirdOpenDate(ebOpen);
                            }
                            if (doc.contains("earlyBirdDeadline")) {
                                ebClose = doc.getDate("earlyBirdDeadline");
                                event.setEarlyBirdDeadline(ebClose);
                            }
                            if (doc.contains("earlyBirdPrice")) {
                                event.setEarlyBirdPrice(doc.getLong("earlyBirdPrice").intValue());
                            }
                            if (doc.contains("earlyBirdLimit")) {
                                event.setEarlyBirdLimit(doc.getLong("earlyBirdLimit").intValue());
                            }
                            if (doc.contains("earlyBirdSold")) {
                                event.setEarlyBirdSold(doc.getLong("earlyBirdSold").intValue());
                            }
                            if (doc.contains("ticketOpenDate")) {
                                event.setTicketOpenDate(doc.getDate("ticketOpenDate"));
                            }
                            if (doc.contains("ticketCloseDate")) {
                                event.setTicketCloseDate(doc.getDate("ticketCloseDate"));
                            }

                            // Cập nhật trạng thái nút đăng ký dựa trên các giai đoạn
                            Date now = new Date();
                            boolean canRegister = false;
                            String statusText = "Thêm vào giỏ hàng";
                            int statusColor = android.graphics.Color.parseColor("#185FA5");
                            String deadlineInfo = "";

                            if (ebOpen != null && ebClose != null && now.after(ebOpen) && now.before(ebClose)) {
                                // Đang trong giai đoạn Early Bird
                                canRegister = true;
                                statusText = "Đặt vé sớm (Early Bird)";
                                deadlineInfo = "Hạn chót mua vé sớm: " + sdfFull.format(ebClose) + " - " + getTimeRemaining(ebClose);
                            } else if (openDate != null && closeDate != null && now.after(openDate) && now.before(closeDate)) {
                                // Đang trong giai đoạn chính thức
                                canRegister = true;
                                deadlineInfo = "Hạn chót đăng ký: " + sdfFull.format(closeDate) + " - " + getTimeRemaining(closeDate);
                            } else if (ebOpen != null && now.before(ebOpen)) {
                                statusText = "CHƯA ĐẾN HẠN BÁN SỚM";
                                statusColor = android.graphics.Color.GRAY;
                                deadlineInfo = "Ngày mở bán sớm: " + sdfFull.format(ebOpen);
                            } else if (openDate != null && now.before(openDate) && (ebClose == null || now.after(ebClose))) {
                                statusText = "CHỜ ĐẾN HẠN BÁN CHÍNH THỨC";
                                statusColor = android.graphics.Color.GRAY;
                                deadlineInfo = "Ngày mở bán chính thức: " + sdfFull.format(openDate);
                            } else if (closeDate != null && now.after(closeDate)) {
                                statusText = "HẾT HẠN ĐĂNG KÝ";
                                statusColor = android.graphics.Color.GRAY;
                                deadlineInfo = "Đã hết hạn đăng ký vào: " + sdfFull.format(closeDate);
                            }

                            btnAddToCart.setEnabled(canRegister);
                            btnAddToCart.setText(statusText);
                            btnAddToCart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));
                            tvDeadline.setText(deadlineInfo);
                            tvDeadline.setVisibility(View.VISIBLE);

                            if (ebOpen != null && ebClose != null) {
                                cardTicketOpen.setVisibility(View.VISIBLE);
                                tvRegPeriod.setText("Sớm: " + sdfPretty.format(ebOpen) + " - " + sdfPretty.format(ebClose) + "\nChính thức: " + (openDate != null ? sdfPretty.format(openDate) : "?") + " - " + (closeDate != null ? sdfPretty.format(closeDate) : "?"));
                            }
                            
                            if (event.getDate() != null) {
                                tvActualDate.setText(sdfPretty.format(event.getDate()));
                                tvDate.setText("Sự kiện diễn ra vào ngày " + sdfFull.format(event.getDate()));
                            }

                            // Hiển thị số lượng vé sớm còn lại
                            if (event.getEarlyBirdPrice() > 0 && event.getEarlyBirdLimit() > 0) {
                                layoutEBRemaining.setVisibility(View.VISIBLE);
                                int ebLeft = event.getEarlyBirdLimit() - event.getEarlyBirdSold();
                                tvEBRemaining.setText(String.valueOf(Math.max(0, ebLeft)));
                            } else {
                                layoutEBRemaining.setVisibility(View.GONE);
                            }

                            // Load Organizer Info
                            if (doc.contains("organizerId")) {
                                String orgId = doc.getString("organizerId");
                                if (orgId != null && !orgId.isEmpty()) {
                                    db.collection("organizations").document(orgId).get().addOnSuccessListener(orgDoc -> {
                                        if (orgDoc.exists()) {
                                            Organization org = orgDoc.toObject(Organization.class);
                                            org.setId(orgDoc.getId());
                                            tvOrgName.setText(org.getName());
                                            if (org.getLogoUrl() != null && !org.getLogoUrl().isEmpty()) {
                                                Glide.with(this).load(org.getLogoUrl()).into(imgOrgLogo);
                                            }
                                            layoutOrg.setOnClickListener(v -> {
                                                OrganizationDetailFragment fragment = OrganizationDetailFragment.newInstance(org);
                                                getParentFragmentManager().beginTransaction()
                                                        .replace(R.id.fragment_container, fragment)
                                                        .addToBackStack(null)
                                                        .commit();
                                            });
                                        }
                                    });
                                }
                            }

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
            
            if (tvRemaining != null) {
                if (event.isLimited()) {
                    if (event.getRemainingTickets() <= 0) {
                        tvRemaining.setText("Hết vé");
                        tvRemaining.setTextColor(android.graphics.Color.RED);
                        btnAddToCart.setEnabled(false);
                        btnAddToCart.setText("ĐÃ HẾT VÉ");
                        btnAddToCart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.GRAY));
                    } else {
                        tvRemaining.setText(String.valueOf(event.getRemainingTickets()));
                        tvRemaining.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
                    }
                } else {
                    tvRemaining.setText("Vô hạn");
                    tvRemaining.setTextColor(android.graphics.Color.parseColor("#1A1A1A"));
                }
            }

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
                    if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        getActivity().finish();
                    }
                }
            });

            btnAddToCart.setOnClickListener(v -> addToCart());

            btnWishlist = view.findViewById(R.id.btnWishlist);
            if (btnWishlist != null) {
                checkWishlistStatus();
                btnWishlist.setOnClickListener(v -> toggleWishlist());
            }

            ImageView btnShareEvent = view.findViewById(R.id.btnShareEvent);
            if (btnShareEvent != null) {
                btnShareEvent.setOnClickListener(v -> showShareDialog());
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
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("COMMENT_ERROR", "Lỗi load bình luận: " + error.getMessage());
                        return;
                    }

                    if (value != null) {
                        List<Comment> comments = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            comments.add(doc.toObject(Comment.class));
                        }

                        // Sắp xếp thủ công theo timestamp tăng dần (để tránh yêu cầu Index trên Firestore)
                        comments.sort((c1, c2) -> {
                            if (c1.getTimestamp() == null || c2.getTimestamp() == null) return 0;
                            return c1.getTimestamp().compareTo(c2.getTimestamp());
                        });

                        layoutCommentsList.removeAllViews();
                        for (Comment comment : comments) {
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

        showAddToCartDialog();
    }

    private int selectedQuantity = 1;
    private TicketType selectedTicketType;

    private void showAddToCartDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);
        dialog.setContentView(dialogView);

        android.widget.Spinner spinner = dialogView.findViewById(R.id.spinnerTicketType);
        TextView tvPrice = dialogView.findViewById(R.id.tvSelectedTicketPrice);
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        Button btnMinus = dialogView.findViewById(R.id.btnMinus);
        Button btnPlus = dialogView.findViewById(R.id.btnPlus);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmAdd);

        List<String> typeNames = new ArrayList<>();
        List<TicketType> availableTypes = new ArrayList<>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date now = new Date();

        // Kiểm tra xem hiện tại đang ở giai đoạn nào
        boolean isEarlyBirdPhase = event.getEarlyBirdOpenDate() != null && event.getEarlyBirdDeadline() != null && 
                                  now.after(event.getEarlyBirdOpenDate()) && now.before(event.getEarlyBirdDeadline());
        
        boolean isOfficialPhase = event.getTicketOpenDate() != null && event.getTicketCloseDate() != null && 
                                 now.after(event.getTicketOpenDate()) && now.before(event.getTicketCloseDate());

        if (isEarlyBirdPhase && event.isEarlyBirdAvailable()) {
            // TRONG THỜI GIAN VÉ SỚM: Chỉ cho chọn vé Early Bird
            String ebName = "Vé sớm (Early Bird)";
            String displayName = ebName + " - " + String.format("%,dđ", event.getEarlyBirdPrice());
            if (event.getEarlyBirdDeadline() != null) {
                displayName += " [Hạn: " + sdf.format(event.getEarlyBirdDeadline()) + "]";
            }
            typeNames.add(displayName);
            
            TicketType ebType = new TicketType(ebName, event.getEarlyBirdPrice(), "Vé đặt sớm với giá ưu đãi");
            ebType.setEarlyBird(true);
            availableTypes.add(ebType);
        } else if (isOfficialPhase) {
            // TRONG THỜI GIAN CHÍNH THỨC: Chỉ cho chọn các loại vé thông thường
            for (TicketType tt : ticketTypesList) {
                if (tt.isAvailable()) {
                    String displayName = tt.getName() + " - " + String.format("%,dđ", tt.getPrice());
                    typeNames.add(displayName);
                    availableTypes.add(tt);
                }
            }
        }

        if (availableTypes.isEmpty()) {
            Toast.makeText(getContext(), "Hiện tại không có loại vé nào khả dụng", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, typeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        selectedQuantity = 1;
        selectedTicketType = availableTypes.get(0);
        tvPrice.setText(String.format("Giá: %,dđ", selectedTicketType.getPrice()));

        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                selectedTicketType = availableTypes.get(position);
                tvPrice.setText(String.format("Giá: %,dđ", selectedTicketType.getPrice() * selectedQuantity));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnMinus.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                tvQuantity.setText(String.valueOf(selectedQuantity));
                tvPrice.setText(String.format("Giá: %,dđ", selectedTicketType.getPrice() * selectedQuantity));
            }
        });

        btnPlus.setOnClickListener(v -> {
            selectedQuantity++;
            tvQuantity.setText(String.valueOf(selectedQuantity));
            tvPrice.setText(String.format("Giá: %,dđ", selectedTicketType.getPrice() * selectedQuantity));
        });

        btnConfirm.setOnClickListener(v -> {
            performAddToCart(dialog);
        });

        dialog.show();
    }

    private void performAddToCart(BottomSheetDialog dialog) {
        String uid = mAuth.getCurrentUser().getUid();
        String eventId = event.getId();

        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("eventId", eventId);
        cartItem.put("title", event.getTitle() + " (" + selectedTicketType.getName() + ")");
        cartItem.put("price", selectedTicketType.getPrice());
        cartItem.put("date", event.getDate());
        cartItem.put("startTime", event.getStartTime());
        cartItem.put("endTime", event.getEndTime());
        cartItem.put("location", event.getLocation());
        cartItem.put("imgUrl", event.getImageUrl());
        cartItem.put("quantity", selectedQuantity);
        cartItem.put("ticketType", selectedTicketType.getName());
        cartItem.put("ticketDescription", selectedTicketType.getDescription());
        cartItem.put("isChosen", true);

        // Use a unique ID for cart items that includes the ticket type to allow different types of the same event in cart
        String cartItemId = eventId + "_" + selectedTicketType.getName();

        db.collection("carts").document(uid)
                .collection("cart_items").document(cartItemId)
                .set(cartItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showShareDialog() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để chia sẻ", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_share_event);
        
        // Set transparent scrim color to avoid dark overlay
        bottomSheetDialog.getWindow().setBackgroundDrawable(null);

        RecyclerView recyclerView = bottomSheetDialog.findViewById(R.id.recyclerShareFriends);
        TextView tvNoFriends = bottomSheetDialog.findViewById(R.id.tvNoFriends);
        ImageView btnCloseShare = bottomSheetDialog.findViewById(R.id.btnCloseShare);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        btnCloseShare.setOnClickListener(v -> bottomSheetDialog.dismiss());

        loadFriendsForShare(recyclerView, tvNoFriends, bottomSheetDialog);

        bottomSheetDialog.show();
    }

    private void loadFriendsForShare(RecyclerView recyclerView, TextView tvNoFriends, BottomSheetDialog dialog) {
        String currentId = mAuth.getCurrentUser().getUid();
        List<Map<String, Object>> friendList = new ArrayList<>();
        List<String> friendIds = new ArrayList<>();

        // Load friends where current user is user1
        db.collection("friends")
                .whereEqualTo("user1", currentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String friendId = doc.getString("user2");
                        if (friendId != null && !friendId.isEmpty()) {
                            friendIds.add(friendId);
                        }
                    }

                    // Also load friends where current user is user2
                    db.collection("friends")
                            .whereEqualTo("user2", currentId)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots2 -> {
                                for (QueryDocumentSnapshot doc : queryDocumentSnapshots2) {
                                    String friendId = doc.getString("user1");
                                    if (friendId != null && !friendId.isEmpty()) {
                                        friendIds.add(friendId);
                                    }
                                }

                                // Now fetch all friend details
                                if (friendIds.isEmpty()) {
                                    updateFriendsList(friendList, recyclerView, tvNoFriends, dialog);
                                } else {
                                    fetchAllFriendDetails(friendIds, 0, friendList, recyclerView, tvNoFriends, dialog);
                                }
                            });
                });
    }

    private void fetchAllFriendDetails(List<String> friendIds, int index, List<Map<String, Object>> friendList, RecyclerView recyclerView, TextView tvNoFriends, BottomSheetDialog dialog) {
        if (index >= friendIds.size()) {
            updateFriendsList(friendList, recyclerView, tvNoFriends, dialog);
            return;
        }

        String friendId = friendIds.get(index);
        db.collection("users").document(friendId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> friend = new HashMap<>();
                        friend.put("uid", friendId);
                        friend.put("fullname", documentSnapshot.getString("fullname"));
                        friend.put("username", documentSnapshot.getString("username"));
                        friend.put("avatarUrl", documentSnapshot.getString("avatarUrl"));
                        friendList.add(friend);
                    }
                    // Fetch next friend
                    fetchAllFriendDetails(friendIds, index + 1, friendList, recyclerView, tvNoFriends, dialog);
                }).addOnFailureListener(e -> {
                    // Even if one fails, continue to the next
                    fetchAllFriendDetails(friendIds, index + 1, friendList, recyclerView, tvNoFriends, dialog);
                });
    }

    private void updateFriendsList(List<Map<String, Object>> friendList, RecyclerView recyclerView, TextView tvNoFriends, BottomSheetDialog dialog) {
        if (friendList.isEmpty()) {
            tvNoFriends.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoFriends.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            ShareEventAdapter adapter = new ShareEventAdapter(getContext(), friendList, (friendId, friendName) -> {
                shareEventWithFriend(friendId, friendName, dialog);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    private void shareEventWithFriend(String friendId, String friendName, BottomSheetDialog dialog) {
        if (mAuth.getCurrentUser() == null || event == null) return;

        String senderId = mAuth.getCurrentUser().getUid();
        String chatId = getChatId(senderId, friendId);

        // Create a message with event information
        String messageText = "🎉 " + event.getTitle() + "\n" +
                "📅 " + event.getFormattedDate() + "\n" +
                "📍 " + event.getLocation() + "\n" +
                "💰 " + event.getPrice() + "đ\n" +
                "\nHãy xem sự kiện này!";

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", senderId);
        message.put("receiverId", friendId);
        message.put("message", messageText);
        message.put("timestamp", System.currentTimeMillis());
        message.put("eventId", event.getId());
        message.put("isEventShare", true);
        message.put("eventTitle", event.getTitle());
        message.put("eventImageUrl", event.getImageUrl());
        message.put("eventDate", event.getFormattedDate());
        message.put("eventPrice", event.getPrice());
        message.put("eventLocation", event.getLocation());

        db.collection("chats").document(chatId).collection("messages").add(message)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Đã chia sẻ sự kiện cho " + friendName, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi khi chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }

    private String getTimeRemaining(Date closeDate) {
        long diff = closeDate.getTime() - new Date().getTime();
        if (diff <= 0) return "Hết hạn";

        long days = diff / (24 * 60 * 60 * 1000);
        long hours = (diff % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
        long minutes = (diff % (60 * 60 * 1000)) / (60 * 1000);

        StringBuilder sb = new StringBuilder("Còn ");
        if (days > 0) sb.append(days).append(" ngày ");
        if (hours > 0) sb.append(hours).append(" giờ ");
        sb.append(minutes).append(" phút");

        return sb.toString();
    }
}