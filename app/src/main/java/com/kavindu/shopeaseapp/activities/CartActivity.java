package com.kavindu.shopeaseapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kavindu.shopeaseapp.adapters.CartAdapter;
import com.kavindu.shopeaseapp.databinding.ActivityCartBinding;
import com.kavindu.shopeaseapp.models.CartItem;
import com.kavindu.shopeaseapp.utils.CartDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity
        implements CartAdapter.CartListener {

    private ActivityCartBinding binding;
    private CartDatabase cartDb;
    private CartAdapter adapter;
    private List<CartItem> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cartDb  = CartDatabase.getInstance(this);
        adapter = new CartAdapter(this, cartItems, this);


        binding.rvCart.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCart.setAdapter(adapter);

        cartDb.cartDao().getAllItems().observe(this, items -> {
            cartItems.clear();
            if (items != null) cartItems.addAll(items);
            adapter.notifyDataSetChanged();

            // Show or hide empty state
            if (cartItems.isEmpty()) {
                binding.rvCart.setVisibility(View.GONE);
                if (binding.tvEmpty != null)
                    binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.rvCart.setVisibility(View.VISIBLE);
                if (binding.tvEmpty != null)
                    binding.tvEmpty.setVisibility(View.GONE);
            }

            // Calculate totals
            double subtotal = 0;
            for (CartItem item : cartItems)
                subtotal += item.getTotalPrice();
            double delivery = cartItems.isEmpty() ? 0 : 350;

            binding.tvSubtotal.setText(
                    String.format("LKR %.2f", subtotal));
            binding.tvDelivery.setText(
                    String.format("LKR %.2f", delivery));
            binding.tvTotal.setText(
                    String.format("LKR %.2f", subtotal + delivery));
            binding.btnCheckout.setEnabled(!cartItems.isEmpty());
        });

        binding.btnCheckout.setOnClickListener(v ->
                startActivity(new Intent(this, CheckoutActivity.class)));
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        item.setQuantity(newQuantity);
        Executors.newSingleThreadExecutor().execute(
                () -> cartDb.cartDao().update(item));
    }

    @Override
    public void onRemove(CartItem item) {
        Executors.newSingleThreadExecutor().execute(
                () -> cartDb.cartDao().delete(item));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}