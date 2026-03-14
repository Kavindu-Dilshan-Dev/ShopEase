package com.kavindu.shopeaseapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderVH> {

    private final Context context;
    private final List<Order> orders;

    public OrderAdapter(Context ctx, List<Order> orders) {
        this.context = ctx;
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderVH h, int pos) {
        Order o = orders.get(pos);
        h.tvOrderId.setText("Order #" + o.getId());
        h.tvAmount.setText(String.format("LKR %.2f", o.getTotalAmount()));
        h.tvItems.setText((o.getItems() != null ? o.getItems().size() : 0) + " item(s)");
        h.tvDate.setText(new SimpleDateFormat("dd MMM yyyy, HH:mm",
                Locale.getDefault()).format(new Date(o.getCreatedAt())));

        String status = o.getStatus() != null ? o.getStatus() : "PENDING";
        h.tvStatus.setText(status);
        h.tvStatus.setBackgroundColor(getStatusColor(status));
    }

    private int getStatusColor(String status) {
        switch (status) {
            case Order.STATUS_CONFIRMED:
                return Color.parseColor("#388E3C");
            case Order.STATUS_SHIPPED:
                return Color.parseColor("#1976D2");
            case Order.STATUS_DELIVERED:
                return Color.parseColor("#00796B");
            case Order.STATUS_CANCELLED:
                return Color.parseColor("#D32F2F");
            default:
                return Color.parseColor("#F57C00");
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderVH extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvAmount, tvItems, tvDate, tvStatus;

        OrderVH(@NonNull View v) {
            super(v);
            tvOrderId = v.findViewById(R.id.tvOrderId);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvItems = v.findViewById(R.id.tvItems);
            tvDate = v.findViewById(R.id.tvDate);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}