package com.kavindu.shopeaseapp;

import static com.kavindu.shopeaseapp.utils.FirestoreSeeder.seedProducts;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;

public class ShopEaseApp extends Application {

    public static final String CHANNEL_ORDERS = "orders_channel";
    public static final String CHANNEL_PROMO   = "promo_channel";
    public static final String CHANNEL_GENERAL = "general_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        createNotificationChannels();
//        seedProducts();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mgr = getSystemService(NotificationManager.class);

            mgr.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ORDERS, "Order Updates", NotificationManager.IMPORTANCE_HIGH));
            mgr.createNotificationChannel(new NotificationChannel(
                    CHANNEL_PROMO, "Promotions", NotificationManager.IMPORTANCE_DEFAULT));
            mgr.createNotificationChannel(new NotificationChannel(
                    CHANNEL_GENERAL, "General", NotificationManager.IMPORTANCE_LOW));
        }
    }
}