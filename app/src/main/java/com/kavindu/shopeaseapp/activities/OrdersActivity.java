package com.kavindu.shopeaseapp.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.*;
import com.kavindu.shopeaseapp.adapters.OrderAdapter;
import com.kavindu.shopeaseapp.databinding.ActivityOrdersBinding;
import com.kavindu.shopeaseapp.models.Order;
import com.kavindu.shopeaseapp.utils.PrefsManager;

import java.util.*;

public class OrdersActivity extends AppCompatActivity {

    private ActivityOrdersBinding binding;
    private FirebaseFirestore db;
    private List<Order> orders = new ArrayList<>();
    private OrderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = FirebaseFirestore.getInstance();
        adapter = new OrderAdapter(this, orders);
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.rvOrders.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        String uid = PrefsManager.getInstance(this).getUserId();
        binding.progressBar.setVisibility(android.view.View.VISIBLE);

        db.collection("orders")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    if (e != null || snapshots == null) return;
                    orders.clear();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) orders.add(order);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(orders.isEmpty()
                            ? android.view.View.VISIBLE : android.view.View.GONE);
                });
    }
}