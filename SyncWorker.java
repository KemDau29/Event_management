package com.example.eventmanagementsystem.service;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.eventmanagementsystem.database.AppDatabase;
import com.example.eventmanagementsystem.model.Order;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        List<Order> orders = db.orderDao().getAllOrders();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        for (Order order : orders) {
            firestore.collection("orders").document(String.valueOf(order.id))
                    .set(order)
                    .addOnFailureListener(e -> {
                        // Log failure
                    });
        }

        return Result.success();
    }
}
