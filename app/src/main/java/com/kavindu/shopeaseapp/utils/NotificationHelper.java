package com.kavindu.shopeaseapp.utils;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.ShopEaseApp;
import com.kavindu.shopeaseapp.activities.OrdersActivity;

public class NotificationHelper {

    public static void showOrderNotification(Context ctx, String orderId, String status) {
        if (!hasNotificationPermission(ctx)) return;

        Intent intent = new Intent(ctx, OrdersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String emoji = status.equals("CONFIRMED") ? "✅" : "❌";
        String msg = status.equals("CONFIRMED")
                ? "Your order " + orderId + " is confirmed! We'll deliver soon."
                : "Order " + orderId + " was cancelled.";

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ctx, ShopEaseApp.CHANNEL_ORDERS)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(emoji + " Order " + status)
                        .setContentText(msg)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pi);

        // ✅ ctx used directly — no "this" needed
        NotificationManagerCompat.from(ctx)
                .notify(orderId.hashCode(), builder.build());
    }

    // ─────────────────────────────────────────────
    // Promo Notification
    // ─────────────────────────────────────────────

    public static void showPromoNotification(Context ctx,
                                             String title,
                                             String message) {
        if (!hasNotificationPermission(ctx)) return;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ctx, ShopEaseApp.CHANNEL_PROMO)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("🎁 " + title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // ✅ ctx used directly — no "this" needed
        NotificationManagerCompat.from(ctx)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    // ─────────────────────────────────────────────
    // General Notification
    // ─────────────────────────────────────────────

    public static void showGeneralNotification(Context ctx,
                                               String title,
                                               String message) {
        if (!hasNotificationPermission(ctx)) return;

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(ctx, ShopEaseApp.CHANNEL_GENERAL)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_LOW);

        // ✅ ctx used directly — no "this" needed
        NotificationManagerCompat.from(ctx)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    // ─────────────────────────────────────────────
    // Permission Check Helper
    // ─────────────────────────────────────────────

    public static boolean hasNotificationPermission(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // ✅ ctx passed directly — no "this" used anywhere
            return ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // Android 12 and below — permission always granted
        return true;
    }
}