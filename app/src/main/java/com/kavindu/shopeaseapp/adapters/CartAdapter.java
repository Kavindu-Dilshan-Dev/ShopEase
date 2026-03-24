package com.kavindu.shopeaseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.models.CartItem;

import java.util.List;

public class CartAdapter extends
        RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemove(CartItem item);
    }

    private final Context context;
    private final List<CartItem> items;
    private final CartListener listener;

    public CartAdapter(Context context,
                       List<CartItem> items,
                       CartListener listener) {
        this.context  = context;
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull CartViewHolder h, int pos) {

        CartItem item = items.get(pos);

        //Set product name
        h.tvName.setText(item.getProductName());

        //Unit price
        h.tvUnitPrice.setText(
                String.format("LKR %.2f each", item.getPrice()));

        // Quantity
        h.tvQuantity.setText(
                String.valueOf(item.getQuantity()));

        // Total price
        h.tvTotalPrice.setText(
                String.format("LKR %.2f", item.getTotalPrice()));

        // Load image
        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.placeholder_product)
                .centerCrop()
                .into(h.ivProduct);

        // Plus
        h.btnPlus.setOnClickListener(v ->
                listener.onQuantityChanged(
                        item, item.getQuantity() + 1));

        // Minus
        h.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                listener.onQuantityChanged(
                        item, item.getQuantity() - 1);
            }
        });

        // Delete
        h.btnDelete.setOnClickListener(v ->
                listener.onRemove(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    static class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView      ivProduct;
        TextView       tvName, tvUnitPrice,
                tvQuantity, tvTotalPrice;
        MaterialButton btnPlus, btnMinus;
        ImageButton    btnDelete;

        CartViewHolder(@NonNull View v) {
            super(v);

            // ✅ All IDs match item_cart.xml exactly
            ivProduct    = v.findViewById(R.id.ivProduct);
            tvName       = v.findViewById(R.id.tvProductName);
            tvUnitPrice  = v.findViewById(R.id.tvUnitPrice);
            tvQuantity   = v.findViewById(R.id.tvQuantity);
            tvTotalPrice = v.findViewById(R.id.tvTotalPrice);
            btnMinus     = v.findViewById(R.id.btnDecrease);
            btnPlus      = v.findViewById(R.id.btnIncrease);
            btnDelete    = v.findViewById(R.id.btnDelete);
        }
    }
}