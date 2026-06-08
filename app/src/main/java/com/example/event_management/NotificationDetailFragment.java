package com.example.event_management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.event_management.models.Notification;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificationDetailFragment extends Fragment {

    private static final String ARG_NOTIFICATION = "arg_notification";

    public static NotificationDetailFragment newInstance(Notification notification) {
        NotificationDetailFragment fragment = new NotificationDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTIFICATION, notification);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_detail, container, false);
        Notification notification = null;
        if (getArguments() != null) {
            notification = (Notification) getArguments().getSerializable(ARG_NOTIFICATION);
        }

        TextView tvTitle = view.findViewById(R.id.tvNotificationDetailTitle);
        TextView tvTimestamp = view.findViewById(R.id.tvNotificationDetailTimestamp);
        TextView tvMessage = view.findViewById(R.id.tvNotificationDetailMessage);
        TextView tvOrderId = view.findViewById(R.id.tvNotificationDetailOrderId);
        TextView tvTicketInfo = view.findViewById(R.id.tvNotificationDetailTicketInfo);
        TextView tvEventInfo = view.findViewById(R.id.tvNotificationDetailEventInfo);
        TextView tvType = view.findViewById(R.id.tvNotificationDetailType);

        if (notification != null) {
            tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Thông báo");
            tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "Không có nội dung.");
            tvTimestamp.setText(formatTimestamp(notification.getTimestamp()));
            tvOrderId.setText(notification.getOrderId() != null ? "Mã đơn: #" + notification.getOrderId() : "Mã đơn: -");
            tvTicketInfo.setText(notification.getTicketInfo() != null ? "Mã vé:\n" + notification.getTicketInfo() : "Mã vé: -");
            tvEventInfo.setText(notification.getEventId() != null ? "Sự kiện: " + notification.getEventId() : "Sự kiện: -");
            tvType.setText("Loại thông báo: " + (notification.getType() != null ? notification.getType() : "Khác"));
        }

        return view;
    }

    private String formatTimestamp(java.util.Date timestamp) {
        if (timestamp == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(timestamp);
    }
}
