package com.kavindu.shopeaseapp.adapters;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemove(CartItem item);
    }

    private final Context context;
    private final List<CartItem> items;
    private final CartListener listener;

    public CartAdapter(Context context, List<CartItem> items, CartListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder h, int pos) {
        CartItem item = items.get(pos);
        h.tvName.setText(item.getProductName());
        h.tvPrice.setText(String.format("LKR %.2f", item.getPrice()));
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));
        h.tvTotal.setText(String.format("LKR %.2f", item.getTotalPrice()));

        Glide.with(context).load(item.getProductImage())
                .placeholder(R.drawable.placeholder_product).into(h.ivProduct);

        h.btnPlus.setOnClickListener(v ->
                listener.onQuantityChanged(item, item.getQuantity() + 1));
        h.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1)
                listener.onQuantityChanged(item, item.getQuantity() - 1);
        });
        h.btnRemove.setOnClickListener(v -> listener.onRemove(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotal;
        ImageButton btnPlus, btnMinus, btnRemove;

        CartViewHolder(@NonNull View v) {
            super(v);
            ivProduct  = v.findViewById(R.id.ivProduct);
            tvName     = v.findViewById(R.id.tvProductName);
            tvPrice    = v.findViewById(R.id.tvUnitPrice);
            tvQuantity = v.findViewById(R.id.tvQuantity);
            tvTotal    = v.findViewById(R.id.tvTotalPrice);
            btnPlus    = v.findViewById(R.id.btnIncrease);
            btnMinus   = v.findViewById(R.id.btnDecrease);
            btnRemove  = v.findViewById(R.id.btnRemove);
        }
    }
}