package com.kavindu.shopeaseapp.fragments;

import android.os.Bundle;
import android.text.*;
import android.view.*;
import android.widget.ArrayAdapter;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.google.firebase.firestore.*;
import com.kavindu.shopeaseapp.adapters.ProductAdapter;
import com.kavindu.shopeaseapp.databinding.FragmentSearchBinding;
import com.kavindu.shopeaseapp.models.CartItem;
import com.kavindu.shopeaseapp.models.Product;
import com.kavindu.shopeaseapp.utils.CartDatabase;

import java.util.*;
import java.util.concurrent.Executors;

public class SearchFragment extends Fragment implements ProductAdapter.OnCartClickListener {

    private FragmentSearchBinding binding;
    private FirebaseFirestore db;
    private List<Product> allProducts = new ArrayList<>();
    private ProductAdapter adapter;
    private CartDatabase cartDb;

    private final String[] CATEGORIES = {
            "All", "Electronics", "Clothing", "Food", "Books", "Sports", "Beauty"
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db     = FirebaseFirestore.getInstance();
        cartDb = CartDatabase.getInstance(requireContext());

        setupRecycler();
        setupCategorySpinner();
        setupSearch();
        loadAllProducts();
    }

    private void setupRecycler() {
        adapter = new ProductAdapter(requireContext(), new ArrayList<>(), this);
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.rvSearchResults.setAdapter(adapter);
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, CATEGORIES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(spinnerAdapter);
        binding.spinnerCategory.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                        filterAndSearch();
                    }
                    @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
                });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { filterAndSearch(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Price range slider
        binding.sliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            binding.tvPriceRange.setText("Max: LKR " + (int) value);
            filterAndSearch();
        });
    }

    private void loadAllProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("products").get()
                .addOnSuccessListener(query -> {
                    allProducts.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) { p.setId(doc.getId()); allProducts.add(p); }
                    }
                    binding.progressBar.setVisibility(View.GONE);
                    filterAndSearch();
                });
    }

    private void filterAndSearch() {
        String query    = binding.etSearch.getText().toString().toLowerCase().trim();
        String category = binding.spinnerCategory.getSelectedItem().toString();
        float  maxPrice = binding.sliderPrice.getValue();

        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            boolean matchName     = query.isEmpty() || p.getName().toLowerCase().contains(query);
            boolean matchCategory = category.equals("All") || p.getCategory().equals(category);
            boolean matchPrice    = p.getPrice() <= maxPrice;
            if (matchName && matchCategory && matchPrice) filtered.add(p);
        }

        adapter.updateList(filtered);
        binding.tvResultCount.setText(filtered.size() + " results");
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
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}