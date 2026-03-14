package com.kavindu.shopeaseapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.work.*;

import com.kavindu.shopeaseapp.workers.SyncWorker;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Schedule periodic sync after device boot
            PeriodicWorkRequest syncWork = new PeriodicWorkRequest.Builder(
                    SyncWorker.class, 6, TimeUnit.HOURS)
                    .setConstraints(new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build())
                    .build();

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    "ShopEaseSync",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    syncWork);
        }
    }
}