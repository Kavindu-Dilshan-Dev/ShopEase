package com.kavindu.shopeaseapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
// ✅ Add this import
import com.google.android.material.button.MaterialButton;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.activities.ProductDetailActivity;
import com.kavindu.shopeaseapp.models.Product;

import java.util.List;

public class ProductAdapter extends
        RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> products;
    private final OnCartClickListener cartClickListener;
    private int lastPosition = -1;

    public interface OnCartClickListener {
        void onAddToCart(Product product);
    }

    public ProductAdapter(Context context,
                          List<Product> products,
                          OnCartClickListener listener) {
        this.context           = context;
        this.products          = products;
        this.cartClickListener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);

        holder.tvProductName.setText(product.getName());
        holder.tvPrice.setText(
                String.format("LKR %.2f", product.getPrice()));
        holder.ratingBar.setRating((float) product.getRating());

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .centerCrop()
                .into(holder.ivProduct);

        // Show discount badge
        if (product.isOnSale()) {
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(String.format("-%.0f%%",
                    (1 - product.getDiscountPrice() /
                            product.getPrice()) * 100));
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        // Click → open product detail
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(
                    context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });

        // Add to cart with scale animation
        holder.btnAddToCart.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.8f).scaleY(0.8f)
                    .setDuration(100)
                    .withEndAction(() ->
                            v.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100)
                                    .start())
                    .start();
            cartClickListener.onAddToCart(product);
        });

        // Favorite button — only if exists in layout
        if (holder.btnFavorite != null) {
            holder.btnFavorite.setOnClickListener(v -> {
                // Handle favorite toggle here if needed
            });
        }

        // Slide-in animation
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            android.view.animation.Animation anim =
                    AnimationUtils.loadAnimation(context,
                            android.R.anim.slide_in_left);
            anim.setDuration(300);
            view.startAnimation(anim);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<Product> newList) {
        products.clear();
        products.addAll(newList);
        notifyDataSetChanged();
    }

    // ── ViewHolder ───────────────────────────
    static class ProductViewHolder extends RecyclerView.ViewHolder {

        ImageView      ivProduct;
        TextView       tvProductName, tvPrice, tvDiscount;
        RatingBar      ratingBar;

        // ✅ Changed from ImageButton to MaterialButton
        MaterialButton btnAddToCart;

        // ✅ btnFavorite stays ImageButton if it's ImageButton in XML
        // If your item_product.xml has MaterialButton for favorite
        // change this to MaterialButton too
        ImageButton    btnFavorite;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct     = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvPrice       = itemView.findViewById(R.id.tvPrice);
            tvDiscount    = itemView.findViewById(R.id.tvDiscount);
            ratingBar     = itemView.findViewById(R.id.ratingBar);

            // ✅ Now correctly typed as MaterialButton
            btnAddToCart  = itemView.findViewById(R.id.btnAddToCart);

            // ✅ Favorite stays ImageButton
            btnFavorite   = itemView.findViewById(R.id.btnFavorite);
        }
    }
}