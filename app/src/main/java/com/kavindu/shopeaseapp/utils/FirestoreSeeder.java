package com.kavindu.shopeaseapp.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.models.Product;

import java.util.*;

/**
 * Run this ONCE from any activity's onCreate() to seed sample products.
 * Call: FirestoreSeeder.seedProducts();
 * Remove the call after first run.
 */
public class FirestoreSeeder {

    public static void seedProducts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        List<Product> products = new ArrayList<>();

        // Electronics
        products.add(make("Wireless Earbuds Pro",
                "High quality wireless earbuds with noise cancellation",
                "Electronics", 4999.99, 4.5, true,
                "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=400"));

        products.add(make("Smart Watch Series 5",
                "Track fitness, notifications and more",
                "Electronics", 12999.00, 4.3, true,
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400"));

        products.add(make("Bluetooth Speaker",
                "Portable 360° surround sound speaker",
                "Electronics", 3499.00, 4.1, false,
                "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400"));

        // Clothing
        products.add(make("Premium Cotton T-Shirt",
                "Comfortable everyday cotton tee, available in all sizes",
                "Clothing", 899.00, 4.0, false,
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400"));

        products.add(make("Slim Fit Jeans",
                "Classic slim fit denim jeans",
                "Clothing", 2499.00, 4.2, true,
                "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400"));

        // Food
        products.add(make("Organic Green Tea (50 bags)",
                "Pure Ceylon organic green tea",
                "Food", 649.00, 4.7, false,
                "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400"));

        products.add(make("Dark Chocolate Box",
                "Assorted premium dark chocolates",
                "Food", 1299.00, 4.6, true,
                "https://images.unsplash.com/photo-1553452118-621e1f860f43?w=400"));

        // Books
        products.add(make("Clean Code",
                "A handbook of Agile Software Craftsmanship by Robert Martin",
                "Books", 3200.00, 4.8, false,
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400"));

        // Sports
        products.add(make("Yoga Mat Premium",
                "Non-slip 6mm thick yoga mat with carrying strap",
                "Sports", 1899.00, 4.4, false,
                "https://images.unsplash.com/photo-1601925228897-0e55e9e6e4e5?w=400"));

        products.add(make("Running Shoes",
                "Lightweight breathable running shoes",
                "Sports", 5999.00, 4.3, true,
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400"));

        // Write all to Firestore
        for (Product p : products) {
            db.collection("products").add(p)
                    .addOnSuccessListener(ref -> System.out.println("Seeded: " + p.getName()))
                    .addOnFailureListener(e -> System.err.println("Seed failed: " + e.getMessage()));
        }

        // Seed a sample promotion
        Map<String, Object> promo = new HashMap<>();
        promo.put("title", "Weekend Sale 🎉");
        promo.put("message", "Get 20% off all Electronics this weekend! Use code: SHOP20");
        promo.put("active", true);
        promo.put("createdAt", System.currentTimeMillis());
        db.collection("promotions").add(promo);
    }

    private static Product make(String name, String desc, String cat,
                                double price, double rating, boolean featured,
                                String imageUrl) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setCategory(cat);
        p.setPrice(price);
        p.setRating(rating);
        p.setFeatured(featured);
        p.setImageUrl(imageUrl);
        p.setStockQuantity(50);
        p.setReviewCount((int)(rating * 20));
        p.setOnSale(featured);
        p.setDiscountPrice(featured ? price * 0.8 : price);
        return p;
    }
}