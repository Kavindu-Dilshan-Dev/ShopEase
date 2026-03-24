package com.kavindu.shopeaseapp.services;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.ShopEaseApp;
import com.kavindu.shopeaseapp.activities.MainActivity;
import com.kavindu.shopeaseapp.utils.PrefsManager;

public class FCMService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Save FCM token to Firestore user document
        String uid = PrefsManager.getInstance(this).getUserId();
        if (!uid.isEmpty()) {
            FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .update("fcmToken", token);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        String title   = "ShopEase";
        String body    = "You have a new notification";
        String channel = ShopEaseApp.CHANNEL_GENERAL;

        if (message.getNotification() != null) {
            title = message.getNotification().getTitle();
            body  = message.getNotification().getBody();
        }

        // Data payload overrides
        if (message.getData().containsKey("title"))   title   = message.getData().get("title");
        if (message.getData().containsKey("body"))    body    = message.getData().get("body");
        if (message.getData().containsKey("type")) {
            String type = message.getData().get("type");
            if ("order".equals(type))  channel = ShopEaseApp.CHANNEL_ORDERS;
            if ("promo".equals(type))  channel = ShopEaseApp.CHANNEL_PROMO;
        }

        showNotification(title, body, channel);
    }

    private void showNotification(String title, String body, String channel) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        NotificationManagerCompat.from(this).notify(
                (int) System.currentTimeMillis(), builder.build());
    }
}