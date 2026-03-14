package com.kavindu.shopeaseapp.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kavindu.shopeaseapp.utils.NotificationHelper;
import com.kavindu.shopeaseapp.utils.PrefsManager;

import java.util.concurrent.TimeUnit;

public class OrderStatusWorker extends Worker {

    public OrderStatusWorker(@NonNull Context context, @NonNull WorkerParameters p) {
        super(context, p);
    }

    @NonNull
    @Override
    public Result doWork() {
        String uid = PrefsManager.getInstance(getApplicationContext()).getUserId();
        if (uid.isEmpty()) return Result.success();

        try {
            QuerySnapshot snapshot = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("orders")
                            .whereEqualTo("userId", uid)
                            .whereEqualTo("status", "SHIPPED")
                            .get(), 30, TimeUnit.SECONDS);


            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                NotificationHelper.showOrderNotification(
                        getApplicationContext(), doc.getId(), "SHIPPED 🚚");
            }
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}