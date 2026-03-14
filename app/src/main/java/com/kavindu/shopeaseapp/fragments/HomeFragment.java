package com.kavindu.shopeaseapp.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.adapters.ProductAdapter;
import com.kavindu.shopeaseapp.databinding.FragmentHomeBinding;
import com.kavindu.shopeaseapp.models.CartItem;
import com.kavindu.shopeaseapp.models.Product;
import com.kavindu.shopeaseapp.utils.CartDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements ProductAdapter.OnCartClickListener {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private List<Product> productList = new ArrayList<>();
    private ProductAdapter adapter;
    private CartDatabase cartDb;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        cartDb = CartDatabase.getInstance(requireContext());

        setupRecyclerViews();
        loadProducts();
        setupSearch();

        binding.swipeRefresh.setOnRefreshListener(this::loadProducts);
    }

    private void setupRecyclerViews() {
        adapter = new ProductAdapter(requireContext(), productList, this);
        binding.rvProducts.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvProducts.setAdapter(adapter);
    }

    private void loadProducts() {
        binding.swipeRefresh.setRefreshing(true);
        db.collection("products")
                .orderBy("name")
                .get()
                .addOnSuccessListener(query -> {
                    productList.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            productList.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    binding.swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void filterProducts(String query) {
        if (query.isEmpty()) {
            adapter.updateList(productList);
            return;
        }
        List<Product> filtered = new ArrayList<>();
        for (Product p : productList)
            if (p.getName().toLowerCase().contains(query.toLowerCase())) filtered.add(p);
        adapter.updateList(filtered);
    }

    @Override
    public void onAddToCart(Product product) {
        Executors.newSingleThreadExecutor().execute(() -> {
            CartItem existing = cartDb.cartDao().getItemByProductId(product.getId());
            if (existing != null) {
                existing.setQuantity(existing.getQuantity() + 1);
                cartDb.cartDao().update(existing);
            } else {
                cartDb.cartDao().insert(new CartItem(
                        product.getId(), product.getName(),
                        product.getImageUrl(), product.getPrice(), 1));
            }
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), product.getName() + " added to cart",
                            Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}