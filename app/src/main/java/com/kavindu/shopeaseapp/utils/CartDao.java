package com.kavindu.shopeaseapp.utils;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.kavindu.shopeaseapp.models.CartItem;

import java.util.List;

@Dao
public interface CartDao {

    @Query("SELECT * FROM cart_items")
    LiveData<List<CartItem>> getAllItems();

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    CartItem getItemByProductId(String productId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CartItem item);

    @Update
    void update(CartItem item);

    @Delete
    void delete(CartItem item);

    @Query("DELETE FROM cart_items")
    void clearCart();

    @Query("SELECT SUM(price * quantity) FROM cart_items")
    LiveData<Double> getTotalPrice();

    @Query("SELECT COUNT(*) FROM cart_items")
    LiveData<Integer> getItemCount();
}