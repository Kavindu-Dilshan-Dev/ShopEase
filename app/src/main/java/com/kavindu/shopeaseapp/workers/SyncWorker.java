package com.kavindu.shopeaseapp.workers;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.utils.NotificationHelper;

public class SyncWorker extends Worker {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) return Result.success();

            // Sync: check for new promos in Firestore
            FirebaseFirestore.getInstance()
                    .collection("promotions")
                    .whereEqualTo("active", true)
                    .get()
                    .addOnSuccessListener(query -> {
                        if (!query.isEmpty()) {
                            String title = query.getDocuments().get(0).getString("title");
                            String msg   = query.getDocuments().get(0).getString("message");
                            NotificationHelper nh = new NotificationHelper(); // changed by me
                            if (title != null && msg != null)
                                nh = new NotificationHelper();
                            nh.showPromoNotification(
                                        getApplicationContext(), title, msg);
                        }
                    });
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}