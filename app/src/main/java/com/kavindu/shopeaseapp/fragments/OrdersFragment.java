package com.kavindu.shopeaseapp.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.firestore.*;
import com.kavindu.shopeaseapp.adapters.OrderAdapter;
import com.kavindu.shopeaseapp.databinding.FragmentOrdersBinding;
import com.kavindu.shopeaseapp.models.Order;
import com.kavindu.shopeaseapp.utils.PrefsManager;

import java.util.*;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private FirebaseFirestore db;
    private List<Order> orders = new ArrayList<>();
    private OrderAdapter adapter;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        adapter = new OrderAdapter(requireContext(), orders);
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrders.setAdapter(adapter);
        loadOrders();
    }

    private void loadOrders() {
        String uid = PrefsManager.getInstance(requireContext()).getUserId();
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("orders")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if (snap == null || e != null) return;
                    orders.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Order o = doc.toObject(Order.class);
                        if (o != null) orders.add(o);
                    }
                    adapter.notifyDataSetChanged();
                    binding.tvEmpty.setVisibility(
                            orders.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}