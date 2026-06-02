package com.example.event_management.helpers;

import android.os.Handler;
import android.os.Looper;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailHelper {
    
    // THÔNG TIN CẤU HÌNH GMAIL (SỬ DỤNG MẬT KHẨU ỨNG DỤNG - APP PASSWORD)
    private static final String SENDER_EMAIL = "namvu2005@gmail.com";
    private static final String SENDER_PASSWORD = "lozm dkla ripi nwmp"; // Không phải mật khẩu đăng nhập thường

    public interface EmailCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public static void sendEmail(String recipient, String subject, String content, EmailCallback callback) {
        // Gửi mail phải thực hiện ở luồng ngầm (Background Thread)
        new Thread(() -> {
            try {
                // Cấu hình các thuộc tính cho Gmail SMTP
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                // Tạo phiên làm việc (Session)
                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                // Tạo nội dung tin nhắn
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);
                message.setText(content);

                // Gửi mail
                Transport.send(message);

                // Thông báo thành công về UI Thread
                new Handler(Looper.getMainLooper()).post(callback::onSuccess);

            } catch (MessagingException e) {
                // In chi tiết lỗi ra Logcat để debug
                android.util.Log.e("EMAIL_ERROR", "Lỗi gửi mail: " + e.getMessage());
                e.printStackTrace();

                // Thông báo thất bại về UI Thread
                new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(e.getMessage()));
            }
        }).start();
    }
}
