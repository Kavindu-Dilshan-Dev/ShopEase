package com.kavindu.shopeaseapp.utils;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.kavindu.shopeaseapp.models.CartItem;

@Database(entities = {CartItem.class}, version = 1, exportSchema = false)
public abstract class CartDatabase extends RoomDatabase {

    private static volatile CartDatabase INSTANCE;

    public abstract CartDao cartDao();

    public static CartDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CartDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            CartDatabase.class,
                            "shopease_cart_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}