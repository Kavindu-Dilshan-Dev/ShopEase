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

import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.adapters.ProductAdapter;
import com.kavindu.shopeaseapp.databinding.FragmentHomeBinding;
import com.kavindu.shopeaseapp.models.CartItem;
import com.kavindu.shopeaseapp.models.Product;
import com.kavindu.shopeaseapp.utils.CartDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment
        implements ProductAdapter.OnCartClickListener {

    private FragmentHomeBinding binding;
    private FirebaseFirestore db;
    private CartDatabase cartDb;
    private ProductAdapter adapter;

    // Two lists — allProducts is never filtered
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(
                inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db     = FirebaseFirestore.getInstance();
        cartDb = CartDatabase.getInstance(requireContext());

        setupRecyclerViews();
        loadProducts();
        setupSearch();

        binding.swipeRefresh.setOnRefreshListener(this::loadProducts);
    }

    // ── Setup RecyclerView ───────────────────
    private void setupRecyclerViews() {
        adapter = new ProductAdapter(
                requireContext(), productList, this);
        binding.rvProducts.setLayoutManager(
                new GridLayoutManager(requireContext(), 2));
        binding.rvProducts.setAdapter(adapter);
    }

    // ── Load Products from Firestore ─────────
    private void loadProducts() {
        binding.swipeRefresh.setRefreshing(true);

        db.collection("products")
                .orderBy("name")
                .get()
                .addOnSuccessListener(query -> {

                    allProducts.clear();
                    productList.clear();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            allProducts.add(p);
                            productList.add(p);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    binding.swipeRefresh.setRefreshing(false);

                    // Setup sections after data is ready
                    setupCategoryChips();
                    loadFeaturedProducts();
                    showMainSections();

                    // Reapply search if user typed during refresh
                    String q = binding.etSearch
                            .getText().toString().trim();
                    if (!q.isEmpty()) filterProducts(q);
                })
                .addOnFailureListener(e -> {
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(),
                            "Failed to load products",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ── Setup Category Chips ─────────────────
    private void setupCategoryChips() {

        // Collect unique categories from products
        List<String> categories = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getCategory() != null
                    && !p.getCategory().isEmpty()
                    && !categories.contains(p.getCategory())) {
                categories.add(p.getCategory());
            }
        }

        // Remove all chips except "All" (index 0)
        int count = binding.chipGroupCategories.getChildCount();
        if (count > 1) {
            binding.chipGroupCategories.removeViews(1, count - 1);
        }

        // Add a chip for each category
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChecked(false);

            // ✅ Get color from your colors.xml directly
            int purple = requireContext().getColor(R.color.purple_700);

            // Outline chip style
            chip.setChipStrokeWidth(2f);
            chip.setChipStrokeColor(
                    android.content.res.ColorStateList.valueOf(purple));

            // Transparent background
            chip.setChipBackgroundColor(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.TRANSPARENT));

            binding.chipGroupCategories.addView(chip);
        }

        // Handle chip selection
        binding.chipGroupCategories
                .setOnCheckedStateChangeListener(
                        (group, checkedIds) -> {
                            if (checkedIds.isEmpty()) return;

                            Chip selected = group.findViewById(
                                    checkedIds.get(0));
                            if (selected == null) return;

                            String selectedCat =
                                    selected.getText().toString();

                            // "All" chip restores everything
                            if (selectedCat.equals("All")) {
                                adapter.updateList(allProducts);
                                binding.tvNoResults
                                        .setVisibility(View.GONE);
                                binding.rvProducts
                                        .setVisibility(View.VISIBLE);
                            } else {
                                filterByCategory(selectedCat);
                            }
                        });

        // Show categories section
        binding.tvCategories.setVisibility(View.VISIBLE);
        binding.categoryScrollView.setVisibility(View.VISIBLE);
    }

    // ── Filter by Category ───────────────────
    private void filterByCategory(String category) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (category.equals(p.getCategory())) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);

        if (filtered.isEmpty()) {
            binding.tvNoResults.setVisibility(View.VISIBLE);
            binding.rvProducts.setVisibility(View.GONE);
        } else {
            binding.tvNoResults.setVisibility(View.GONE);
            binding.rvProducts.setVisibility(View.VISIBLE);
        }
    }

    // ── Load Featured Products ───────────────
    private void loadFeaturedProducts() {
        List<Product> featured = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.isFeatured()) featured.add(p);
        }

        if (!featured.isEmpty()) {
            binding.tvFeatured.setVisibility(View.VISIBLE);
            binding.rvFeatured.setVisibility(View.VISIBLE);

            ProductAdapter featuredAdapter =
                    new ProductAdapter(
                            requireContext(), featured, this);
            binding.rvFeatured.setLayoutManager(
                    new androidx.recyclerview.widget.LinearLayoutManager(
                            requireContext(),
                            androidx.recyclerview.widget.LinearLayoutManager
                                    .HORIZONTAL,
                            false));
            binding.rvFeatured.setAdapter(featuredAdapter);
        } else {
            binding.tvFeatured.setVisibility(View.GONE);
            binding.rvFeatured.setVisibility(View.GONE);
        }
    }

    // ── Show All Main Sections ───────────────
    private void showMainSections() {
        binding.tvAllProducts.setVisibility(View.VISIBLE);
        binding.tvNoResults.setVisibility(View.GONE);
        binding.rvProducts.setVisibility(View.VISIBLE);
    }

    // ── Search Setup ─────────────────────────
    private void setupSearch() {
        binding.etSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int st, int c, int a) {}

                    @Override
                    public void onTextChanged(
                            CharSequence s, int st, int b, int c) {
                        filterProducts(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
    }

    // ── Filter Products by Search ────────────
    private void filterProducts(String query) {
        String trimmed = query.trim();

        if (trimmed.isEmpty()) {
            // Restore all products and sections
            adapter.updateList(allProducts);
            binding.tvCategories.setVisibility(View.VISIBLE);
            binding.categoryScrollView
                    .setVisibility(View.VISIBLE);
            binding.tvFeatured.setVisibility(
                    binding.rvFeatured.getVisibility());
            binding.rvFeatured.setVisibility(
                    binding.rvFeatured.getVisibility());
            binding.tvNoResults.setVisibility(View.GONE);
            binding.rvProducts.setVisibility(View.VISIBLE);

            // Reset chip selection to "All"
            binding.chipAll.setChecked(true);
            return;
        }

        // Hide sections when searching
        binding.tvCategories.setVisibility(View.GONE);
        binding.categoryScrollView.setVisibility(View.GONE);
        binding.tvFeatured.setVisibility(View.GONE);
        binding.rvFeatured.setVisibility(View.GONE);

        // Search in name, category and description
        String lower = trimmed.toLowerCase();
        List<Product> filtered = new ArrayList<>();

        for (Product p : allProducts) {
            boolean matchName =
                    p.getName() != null &&
                            p.getName().toLowerCase().contains(lower);

            boolean matchCat =
                    p.getCategory() != null &&
                            p.getCategory().toLowerCase().contains(lower);

            boolean matchDesc =
                    p.getDescription() != null &&
                            p.getDescription().toLowerCase().contains(lower);

            if (matchName || matchCat || matchDesc) {
                filtered.add(p);
            }
        }

        adapter.updateList(filtered);

        if (filtered.isEmpty()) {
            binding.tvNoResults.setVisibility(View.VISIBLE);
            binding.rvProducts.setVisibility(View.GONE);
        } else {
            binding.tvNoResults.setVisibility(View.GONE);
            binding.rvProducts.setVisibility(View.VISIBLE);
        }
    }

    // ── Add to Cart ──────────────────────────
    @Override
    public void onAddToCart(Product product) {
        Executors.newSingleThreadExecutor().execute(() -> {
            CartItem existing = cartDb.cartDao()
                    .getItemByProductId(product.getId());

            if (existing != null) {
                existing.setQuantity(
                        existing.getQuantity() + 1);
                cartDb.cartDao().update(existing);
            } else {
                cartDb.cartDao().insert(new CartItem(
                        product.getId(),
                        product.getName(),
                        product.getImageUrl(),
                        product.getPrice(),
                        1));
            }

            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(),
                            product.getName() + " added to cart ✅",
                            Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}