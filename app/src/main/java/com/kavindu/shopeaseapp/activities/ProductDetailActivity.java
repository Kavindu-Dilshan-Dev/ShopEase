package com.kavindu.shopeaseapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.databinding.ActivityProductDetailBinding;
import com.kavindu.shopeaseapp.models.CartItem;
import com.kavindu.shopeaseapp.models.Product;
import com.kavindu.shopeaseapp.utils.CartDatabase;

import java.util.concurrent.Executors;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private Product product;
    private CartDatabase cartDb;
    private int quantity = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartDb = CartDatabase.getInstance(this);

        // Get product passed from adapter
        product = (Product) getIntent().getSerializableExtra("product");

        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        populateUI();
        setupQuantityControls();
        setupButtons();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }



    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(product.getName());
        }
    }

    private void populateUI() {
        // Product image
        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .centerCrop()
                .into(binding.ivProduct);

        // Product details
        binding.tvProductName.setText(product.getName());
        binding.tvPrice.setText(String.format("LKR %.2f", product.getPrice()));
        binding.tvDescription.setText(product.getDescription());
        binding.ratingBar.setRating((float) product.getRating());
        binding.tvRatingCount.setText(
                "(" + product.getReviewCount() + " reviews)");
        binding.tvQuantity.setText(String.valueOf(quantity));

        // Stock availability
        if (product.getStockQuantity() <= 0) {
            binding.btnAddToCart.setEnabled(false);
            binding.btnBuyNow.setEnabled(false);
            binding.btnAddToCart.setText("OUT OF STOCK");
        }
    }



    private void setupQuantityControls() {
        binding.btnIncrease.setOnClickListener(v -> {
            if (quantity < product.getStockQuantity()) {
                quantity++;
                binding.tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(this,
                        "Maximum stock reached", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
            }
        });
    }

    // ─────────────────────────────────────────────
    // Buttons
    // ─────────────────────────────────────────────

    private void setupButtons() {
        // Add to Cart
        binding.btnAddToCart.setOnClickListener(v -> addToCart());

        // Buy Now — add to cart then go to checkout
        binding.btnBuyNow.setOnClickListener(v -> {
            addToCart();
            startActivity(new Intent(this, CheckoutActivity.class));
        });
    }



    private void addToCart() {
        Executors.newSingleThreadExecutor().execute(() -> {
            CartItem existing = cartDb.cartDao()
                    .getItemByProductId(product.getId());

            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + quantity);
                cartDb.cartDao().update(existing);
            } else {
                CartItem newItem = new CartItem(
                        product.getId(),
                        product.getName(),
                        product.getImageUrl(),
                        product.getPrice(),
                        quantity
                );
                cartDb.cartDao().insert(newItem);
            }

            runOnUiThread(() ->
                    Toast.makeText(this,
                            product.getName() + " added to cart ✓",
                            Toast.LENGTH_SHORT).show());
        });
    }
}