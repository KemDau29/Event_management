package com.example.event_management.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.event_management.R;
import com.example.event_management.helpers.EmailHelper;
import com.example.event_management.models.Ticket;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends BaseAdapter {

    private Context context;
    private List<Ticket> ticketList = new ArrayList<>();
    private FirebaseFirestore db;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public TicketAdapter(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setTicketList(List<Ticket> ticketList) {
        this.ticketList = ticketList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() { return ticketList.size(); }

    @Override
    public Object getItem(int i) { return ticketList.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_ticket, viewGroup, false);
        }

        Ticket ticket = ticketList.get(i);

        TextView tvTitle = view.findViewById(R.id.tvTicketTitle);
        TextView tvCode = view.findViewById(R.id.tvTicketCode);
        TextView tvDate = view.findViewById(R.id.tvPurchaseDate);
        TextView tvEventTime = view.findViewById(R.id.tvEventTime);
        TextView tvQuantity = view.findViewById(R.id.tvTicketQuantity);
        TextView tvTotal = view.findViewById(R.id.tvTicketTotalPrice);
        TextView tvRecipientInfo = view.findViewById(R.id.tvRecipientInfo);
        TextView tvStatus = view.findViewById(R.id.tvTicketStatus);
        TextView tvOngoingNote = view.findViewById(R.id.tvOngoingNote);
        View layoutActions = view.findViewById(R.id.layoutActionButtons);
        Button btnTransfer = view.findViewById(R.id.btnTransferTicket);
        Button btnCancel = view.findViewById(R.id.btnCancelTicket);

        tvTitle.setText(ticket.getTitle());
        tvCode.setText("#" + (ticket.getConfirmCode() != null ? ticket.getConfirmCode() : "N/A"));
        tvDate.setText(ticket.getPurchaseDate() != null ? sdf.format(ticket.getPurchaseDate()) : "");
        
        if (ticket.getStartTime() != null) {
            tvEventTime.setText(sdf.format(ticket.getStartTime()));
        } else if (ticket.getEventDate() != null) {
            tvEventTime.setText(sdf.format(ticket.getEventDate()));
        } else {
            tvEventTime.setText("N/A");
        }

        tvQuantity.setText(String.format(Locale.getDefault(), "%02d", ticket.getQuantity()));
        tvTotal.setText(String.format(Locale.getDefault(), "%,d đ", (long)ticket.getPrice() * ticket.getQuantity()));

        java.util.Date now = new java.util.Date();
        java.util.Date compareStart = ticket.getStartTime() != null ? ticket.getStartTime() : ticket.getEventDate();
        java.util.Date compareEnd = ticket.getEndTime();
        
        if (compareEnd == null && ticket.getEventDate() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(ticket.getEventDate());
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            compareEnd = cal.getTime();
        }

        boolean isOngoing = compareStart != null && compareEnd != null && 
                          now.after(compareStart) && now.before(compareEnd);

        if (isOngoing && "Đã mua".equals(ticket.getStatus())) {
            tvStatus.setText("Đang diễn ra");
            tvStatus.setTextColor(android.graphics.Color.parseColor("#185FA5"));
            tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F0F7FF")));
            
            layoutActions.setVisibility(View.GONE);
            tvOngoingNote.setVisibility(View.VISIBLE);
            tvRecipientInfo.setVisibility(View.GONE);

            view.setOnClickListener(v -> showEventTimelineDialog(ticket.getEventId()));
        } else {
            view.setOnClickListener(null);
            tvOngoingNote.setVisibility(View.GONE);
            layoutActions.setVisibility(View.VISIBLE);

            if ("Đã mua".equals(ticket.getStatus())) {
                tvStatus.setText("Đã mua");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E8F5E9")));
                btnTransfer.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                tvRecipientInfo.setVisibility(View.GONE);
            } else if ("Đã bán".equals(ticket.getStatus())) {
                tvStatus.setText("Đã bán");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#888888"));
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F5F5F5")));
                btnTransfer.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                tvRecipientInfo.setVisibility(View.VISIBLE);
                tvRecipientInfo.setText("Đã chuyển cho: " + ticket.getRecipientUsername());
            } else { // Đã hủy
                tvStatus.setText("Đã hủy");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#B71C1C"));
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFEBEE")));
                btnTransfer.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                tvRecipientInfo.setVisibility(View.GONE);
            }
        }

        btnTransfer.setOnClickListener(v -> showTransferDialog(ticket));
        btnCancel.setOnClickListener(v -> showCancelConfirmDialog(ticket));

        return view;
    }

    private void showTransferDialog(Ticket ticket) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_transfer_ticket, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText edtRecipientUsername = dialogView.findViewById(R.id.edtRecipientUsername);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelTransfer);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmTransfer);
        View btnClose = dialogView.findViewById(R.id.btnCloseDialog);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String recipientUsername = edtRecipientUsername.getText().toString().trim();
            if (recipientUsername.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập username người nhận", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            checkRecipientAndConfirm(ticket, recipientUsername);
        });

        dialog.show();
    }

    private void checkRecipientAndConfirm(Ticket ticket, String username) {
        db.collection("users").whereEqualTo("username", username).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(context, "Không tìm thấy người dùng này!", Toast.LENGTH_SHORT).show();
                    } else {
                        QueryDocumentSnapshot recipientDoc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        String recipientUid = recipientDoc.getId();
                        
                        if (recipientUid.equals(ticket.getUserId())) {
                            Toast.makeText(context, "Bạn không thể chuyển vé cho chính mình!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        View confirmView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_confirm, null);
                        AlertDialog confirmDialog = new AlertDialog.Builder(context)
                                .setView(confirmView)
                                .create();
                        
                        confirmDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                        TextView tvMsg = confirmView.findViewById(R.id.tvConfirmMessage);
                        tvMsg.setText("Bạn có chắc chắn chuyển vé cho '" + username + "' đó chứ?");
                        
                        confirmView.findViewById(R.id.btnNo).setOnClickListener(v -> confirmDialog.dismiss());
                        confirmView.findViewById(R.id.btnYes).setOnClickListener(v -> {
                            confirmDialog.dismiss();
                            transferTicket(ticket, recipientUid, username);
                        });
                        
                        confirmDialog.show();
                    }
                });
    }

    private void transferTicket(Ticket ticket, String recipientUid, String recipientUsername) {
        // Cập nhật vé cũ thành "Đã bán"
        db.collection("tickets").document(ticket.getTicketId())
                .update("status", "Đã bán", "recipientUsername", recipientUsername)
                .addOnSuccessListener(aVoid -> {
                    // Tạo một vé mới cho người nhận
                    String newTicketId = db.collection("tickets").document().getId();
                    Ticket newTicket = new Ticket();
                    newTicket.setTicketId(newTicketId);
                    newTicket.setOrderId(ticket.getOrderId());
                    newTicket.setEventId(ticket.getEventId());
                    newTicket.setUserId(recipientUid);
                    newTicket.setPurchaserId(ticket.getPurchaserId());
                    newTicket.setTitle(ticket.getTitle());
                    newTicket.setPrice(ticket.getPrice());
                    newTicket.setEventDate(ticket.getEventDate());
                    newTicket.setPurchaseDate(ticket.getPurchaseDate());
                    newTicket.setLocation(ticket.getLocation());
                    newTicket.setImgUrl(ticket.getImgUrl());
                    newTicket.setQuantity(ticket.getQuantity());
                    newTicket.setStatus("Đã mua");
                    newTicket.setConfirmCode(ticket.getConfirmCode());

                    db.collection("tickets").document(newTicketId).set(newTicket)
                            .addOnSuccessListener(aVoid1 -> {
                                sendTransferEmails(ticket, recipientUid, recipientUsername);
                                showSuccessDialog();
                            });
                });
    }

    private void sendTransferEmails(Ticket ticket, String recipientUid, String recipientUsername) {
        // 1. Lấy email người gửi (User hiện tại)
        db.collection("users").document(ticket.getUserId()).get().addOnSuccessListener(senderDoc -> {
            String senderEmail = senderDoc.getString("email");
            String senderName = senderDoc.getString("fullname");

            // 2. Lấy email người nhận
            db.collection("users").document(recipientUid).get().addOnSuccessListener(recipientDoc -> {
                String recipientEmail = recipientDoc.getString("email");

                if (senderEmail != null) {
                    String subjectSender = "Xác nhận chuyển vé thành công";
                    String contentSender = "Chào " + senderName + ",\n\n" +
                            "Bạn đã chuyển thành công vé '" + ticket.getTitle() + "' cho người dùng '" + recipientUsername + "'.\n" +
                            "Mã xác nhận vé: #" + ticket.getConfirmCode() + "\n\n" +
                            "Trân trọng!";
                    EmailHelper.sendEmail(senderEmail, subjectSender, contentSender, new EmailHelper.EmailCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String error) {}
                    });
                }

                if (recipientEmail != null) {
                    String subjectRecipient = "Bạn nhận được vé sự kiện mới";
                    String contentRecipient = "Chào " + recipientUsername + ",\n\n" +
                            "Bạn vừa nhận được vé sự kiện '" + ticket.getTitle() + "' từ người dùng '" + (senderName != null ? senderName : "ẩn danh") + "'.\n" +
                            "Mã xác nhận vé của bạn là: #" + ticket.getConfirmCode() + "\n" +
                            "Vui lòng kiểm tra trong mục 'Vé của tôi' trên ứng dụng.\n\n" +
                            "Trân trọng!";
                    EmailHelper.sendEmail(recipientEmail, subjectRecipient, contentRecipient, new EmailHelper.EmailCallback() {
                        @Override public void onSuccess() {}
                        @Override public void onFailure(String error) {}
                    });
                }
            });
        });
    }

    private void showSuccessDialog() {
        View successView = LayoutInflater.from(context).inflate(R.layout.dialog_custom_success, null);
        AlertDialog successDialog = new AlertDialog.Builder(context)
                .setView(successView)
                .create();
        
        successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        successDialog.show();

        // Tự động đóng sau 2 giây
        successView.postDelayed(successDialog::dismiss, 2000);
    }

    private void showCancelConfirmDialog(Ticket ticket) {
        new AlertDialog.Builder(context)
                .setTitle("Hủy vé")
                .setMessage("Bạn có chắc chắn muốn hủy vé này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    db.collection("tickets").document(ticket.getTicketId())
                            .update("status", "Đã hủy")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(context, "Đã hủy vé thành công!", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void showEventTimelineDialog(String eventId) {
        if (eventId == null) return;

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_event_timeline, null);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        RecyclerView rvTimeline = dialogView.findViewById(R.id.rvTimelineDialog);
        TextView tvTitle = dialogView.findViewById(R.id.tvTimelineDialogTitle);
        TextView tvLocation = dialogView.findViewById(R.id.tvTimelineLocation);
        Button btnViewDetail = dialogView.findViewById(R.id.btnViewEventDetail);
        View btnClose = dialogView.findViewById(R.id.btnCloseTimeline);

        rvTimeline.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(context));
        List<com.example.event_management.models.TimelineItem> timelineItems = new ArrayList<>();
        TimelineAdapter adapter = new TimelineAdapter(timelineItems);
        rvTimeline.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        db.collection("events").document(eventId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                com.example.event_management.models.Event event = doc.toObject(com.example.event_management.models.Event.class);
                if (event != null) {
                    event.setId(doc.getId());
                    tvTitle.setText("Lịch trình: " + event.getTitle());
                    tvLocation.setText("📍 Địa điểm: " + event.getLocation());
                    
                    btnViewDetail.setOnClickListener(v -> {
                        dialog.dismiss();
                        if (context instanceof androidx.fragment.app.FragmentActivity) {
                            androidx.fragment.app.FragmentActivity activity = (androidx.fragment.app.FragmentActivity) context;
                            activity.getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, com.example.event_management.event_detail.newInstance(event))
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });

                    List<HashMap<String, Object>> timelineData = (List<HashMap<String, Object>>) doc.get("timeline");
                    if (timelineData != null) {
                        for (HashMap<String, Object> item : timelineData) {
                            com.example.event_management.models.TimelineItem ti = new com.example.event_management.models.TimelineItem();
                            
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
                            timelineItems.add(ti);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        dialog.show();
    }
}
