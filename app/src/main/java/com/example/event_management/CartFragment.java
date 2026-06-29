package com.example.event_management;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.event_management.adapters.CartAdapter;
import com.example.event_management.helpers.EmailHelper;
import com.example.event_management.models.CartItem;
import com.example.event_management.models.Order;
import com.example.event_management.models.Ticket;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CartFragment extends Fragment {

    private TextView tvTotalCartPrice, tvSubtotal, tvDiscountAmount, btnDeleteSelected;
    private EditText edtCoupon;
    private Button btnCheckout, btnApplyCoupon;
    private View layoutDiscount;
    private List<String> usedEmails = new ArrayList<>();
    private CartAdapter adapter;
    private List<CartItem> cartItemList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private long currentTotal = 0;
    private long subtotal = 0;
    private double discountPercent = 0.0;

    public CartFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        tvTotalCartPrice = view.findViewById(R.id.tvTotalCartPrice);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvDiscountAmount = view.findViewById(R.id.tvDiscountAmount);
        layoutDiscount = view.findViewById(R.id.layoutDiscount);
        edtCoupon = view.findViewById(R.id.edtCoupon);
        btnApplyCoupon = view.findViewById(R.id.btnApplyCoupon);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        btnDeleteSelected = view.findViewById(R.id.btnDeleteSelected);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ListView listCartItems = view.findViewById(R.id.listCartItems);
        cartItemList = new ArrayList<>();
        adapter = new CartAdapter(requireContext(), () -> calculateTotalPrice());
        listCartItems.setAdapter(adapter);

        if (mAuth.getCurrentUser() != null) {
            loadCartItems();
            loadUsedEmails();
        }

        view.findViewById(R.id.btnBackCart).setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        btnApplyCoupon.setOnClickListener(v -> {
            db.collection("voucher").get().addOnSuccessListener(documentSnapshot -> {
                String code = edtCoupon.getText().toString().trim();
                if (code.equalsIgnoreCase("DIS10")) {
                    discountPercent = 0.1;
                    layoutDiscount.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Đã áp dụng mã giảm giá 10%!", Toast.LENGTH_SHORT).show();
                } else {
                    discountPercent = 0.0;
                    layoutDiscount.setVisibility(View.GONE);
                    if (!code.isEmpty()) {
                        Toast.makeText(getContext(), "Mã giảm giá không hợp lệ!", Toast.LENGTH_SHORT).show();
                    }
                }
                calculateTotalPrice();
            });
        });

        btnDeleteSelected.setOnClickListener(v -> deleteSelectedItems());

        btnCheckout.setOnClickListener(v -> {
            List<CartItem> chosenItems = new ArrayList<>();
            for (CartItem item : cartItemList) {
                if (item.isChosen()) {
                    chosenItems.add(item);
                }
            }

            if (chosenItems.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ít nhất một sự kiện để thanh toán!", Toast.LENGTH_SHORT).show();
                return;
            }

            showCheckoutConfirmDialog(chosenItems);
        });

        return view;
    }

    private void showCheckoutConfirmDialog(List<CartItem> chosenItems) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_checkout_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        TextView tvOrderId = dialogView.findViewById(R.id.tvDialogOrderId);
        LinearLayout layoutItems = dialogView.findViewById(R.id.layoutDialogItems);
        TextView tvTotal = dialogView.findViewById(R.id.tvDialogTotal);
        Button btnCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnDialogConfirm);
        AutoCompleteTextView edtEmailConfirm = dialogView.findViewById(R.id.edtEmailConfirm);

        // Đổ dữ liệu gợi ý vào AutoCompleteTextView
        ArrayAdapter<String> emailAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, usedEmails);
        edtEmailConfirm.setAdapter(emailAdapter);
        
        // Hiện danh sách ngay khi nhấn vào
        edtEmailConfirm.setOnClickListener(v -> edtEmailConfirm.showDropDown());
        edtEmailConfirm.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) edtEmailConfirm.showDropDown();
        });

        // Mặc định điền email hiện tại của user
        if (mAuth.getCurrentUser() != null) {
            edtEmailConfirm.setText(mAuth.getCurrentUser().getEmail());
        }

        // Tạo mã đơn hàng
        String tempOrderId = "ORD" + System.currentTimeMillis() % 1000000;
        tvOrderId.setText("Mã đơn hàng: #" + tempOrderId);
        tvTotal.setText(String.format(java.util.Locale.getDefault(), "%dđ", currentTotal));

        // Đổ danh sách sản phẩm vào dialog
        for (CartItem item : chosenItems) {
            View itemView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, layoutItems, false);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);
            
            text1.setText(item.getTitle());
            text1.setTextColor(getResources().getColor(android.R.color.black));
            text2.setText(String.format("SL: %d | Giá: %dđ", item.getQuantity(), item.getPrice()));
            
            layoutItems.addView(itemView);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String selectedEmail = edtEmailConfirm.getText().toString().trim();
            if (selectedEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(selectedEmail).matches()) {
                Toast.makeText(getContext(), "Vui lòng nhập email hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            processCheckout(chosenItems, selectedEmail);
        });

        dialog.show();
    }

    private void loadUsedEmails() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> emails = (List<String>) documentSnapshot.get("usedEmails");
                usedEmails.clear();
                if (emails != null) {
                    usedEmails.addAll(emails);
                }
                // Luôn thêm email đăng ký vào danh sách nếu chưa có
                String primaryEmail = mAuth.getCurrentUser().getEmail();
                if (primaryEmail != null && !usedEmails.contains(primaryEmail)) {
                    usedEmails.add(0, primaryEmail);
                }
            }
        });
    }

    private void processCheckout(List<CartItem> chosenItems, String targetEmail) {
        String uid = mAuth.getCurrentUser().getUid();
        String orderId = db.collection("orders").document().getId();

        Random random = new Random();
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Chào bạn,\n\nĐơn hàng #").append(orderId).append(" của bạn đã thanh toán thành công.\n\n");
        emailBody.append("Chi tiết mã xác nhận vé:\n");

        for (CartItem item : chosenItems) {
            String code = String.format("%06d", random.nextInt(1000000));
            item.setConfirmCode(code);
            emailBody.append("- ").append(item.getTitle()).append(": ").append(code).append("\n");
        }

        emailBody.append("\nTổng thanh toán: ").append(currentTotal).append("đ\n");
        emailBody.append("Vui lòng xuất trình mã này tại quầy để nhận vé.\n\nTrân trọng!");

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(uid);
        order.setItems(chosenItems);
        order.setTotalPrice(currentTotal);
        order.setOrderDate(new Date());
        order.setStatus("Thành công");

        WriteBatch batch = db.batch();
        
        // 1. Lưu đơn hàng
        batch.set(db.collection("orders").document(orderId), order);

        // 2. Xóa giỏ hàng và 3. Tạo vé riêng lẻ
        for (CartItem item : chosenItems) {
            batch.delete(db.collection("carts").document(uid)
                    .collection("cart_items").document(item.getCartItemId()));
            
            // Cập nhật số lượng vé còn lại nếu sự kiện có giới hạn
            DocumentReference eventRef = db.collection("events").document(item.getEventId());
            eventRef.get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Boolean isLimited = doc.getBoolean("limited"); // Tên field trong Firestore có thể là 'limited' hoặc 'isLimited'
                    if (isLimited != null && isLimited) {
                        eventRef.update("remainingTickets", com.google.firebase.firestore.FieldValue.increment(-item.getQuantity()));
                    }
                }
            });

            // Tạo đối tượng Ticket
            String ticketId = db.collection("tickets").document().getId();
            Ticket ticket = new Ticket();
            ticket.setTicketId(ticketId);
            ticket.setOrderId(orderId);
            ticket.setEventId(item.getEventId());
            ticket.setUserId(uid);
            ticket.setPurchaserId(uid);
            ticket.setTitle(item.getTitle());
            ticket.setPrice(item.getPrice());
            ticket.setEventDate(item.getDate());
            ticket.setPurchaseDate(new Date());
            ticket.setLocation(item.getLocation());
            ticket.setImgUrl(item.getImageUrl());
            ticket.setQuantity(item.getQuantity());
            ticket.setTicketType(item.getTicketType());
            ticket.setStatus("Đã mua");
            ticket.setConfirmCode(item.getConfirmCode());
            
            batch.set(db.collection("tickets").document(ticketId), ticket);
        }

        // 4. Cập nhật danh sách email đã dùng
        if (!usedEmails.contains(targetEmail)) {
            usedEmails.add(targetEmail);
            batch.update(db.collection("users").document(uid), "usedEmails", usedEmails);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Thanh toán thành công! Đang gửi mã vé...", Toast.LENGTH_SHORT).show();
            
            // Gửi email tự động thông qua API
            EmailHelper.sendEmail(targetEmail, "Xác nhận vé sự kiện #" + orderId, emailBody.toString(), new EmailHelper.EmailCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Mã vé đã được gửi tới email: " + targetEmail, Toast.LENGTH_LONG).show();
                    
                    // Chuyển sang màn hình thành công
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, PaymentSuccessFragment.newInstance(chosenItems))
                            .commit();
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(getContext(), "Gửi mail thất bại. Vui lòng xem trong Lịch sử.", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, PaymentSuccessFragment.newInstance(chosenItems))
                            .commit();
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Thanh toán thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCartItems() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("carts").document(uid)
                .collection("cart_items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        cartItemList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            CartItem item = doc.toObject(CartItem.class);
                            item.setCartItemId(doc.getId());
                            cartItemList.add(item);
                        }
                        adapter.setCartItemList(cartItemList);
                        calculateTotalPrice();
                    }
                });
    }

    private void deleteSelectedItems() {
        if (mAuth.getCurrentUser() == null) return;

        List<CartItem> itemsToDelete = new ArrayList<>();
        for (CartItem item : cartItemList) {
            if (item.isChosen()) {
                itemsToDelete.add(item);
            }
        }

        if (itemsToDelete.isEmpty()) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Xóa mục đã chọn")
                .setMessage("Bạn có chắc chắn muốn xóa " + itemsToDelete.size() + " mục đã chọn khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    String uid = mAuth.getCurrentUser().getUid();
                    WriteBatch batch = db.batch();
                    for (CartItem item : itemsToDelete) {
                        batch.delete(db.collection("carts").document(uid)
                                .collection("cart_items").document(item.getCartItemId()));
                    }
                    batch.commit().addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Đã xóa các mục đã chọn", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void calculateTotalPrice() {
        subtotal = 0;
        boolean hasChosenItem = false;

        for (CartItem item : cartItemList) {
            if (item.isChosen()) {
                subtotal += (long) item.getPrice() * item.getQuantity();
                hasChosenItem = true;
            }
        }

        long discountAmount = (long) (subtotal * discountPercent);
        currentTotal = subtotal - discountAmount;

        tvSubtotal.setText(String.format(java.util.Locale.getDefault(), "%dđ", subtotal));
        tvDiscountAmount.setText(String.format(java.util.Locale.getDefault(), "-%dđ", discountAmount));
        tvTotalCartPrice.setText(String.format(java.util.Locale.getDefault(), "%dđ", currentTotal));

        if (hasChosenItem) {
            btnCheckout.setEnabled(true);
            btnCheckout.setAlpha(1.0f);
            btnDeleteSelected.setVisibility(View.VISIBLE);
        } else {
            btnCheckout.setEnabled(false);
            btnCheckout.setAlpha(0.5f);
            btnDeleteSelected.setVisibility(View.GONE);
        }
    }
}
