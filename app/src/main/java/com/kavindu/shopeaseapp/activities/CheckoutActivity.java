package com.kavindu.shopeaseapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.databinding.ActivityCheckoutBinding;
import com.kavindu.shopeaseapp.models.CartItem;
import com.kavindu.shopeaseapp.models.Order;
import com.kavindu.shopeaseapp.utils.CartDatabase;
import com.kavindu.shopeaseapp.utils.NotificationHelper;
import com.kavindu.shopeaseapp.utils.PrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutActivity extends AppCompatActivity {

    private static final int PAYHERE_REQUEST = 11001;

    private ActivityCheckoutBinding binding;
    private FirebaseFirestore db;
    private PrefsManager prefs;
    private CartDatabase cartDb;
    private List<CartItem> cartItems = new ArrayList<>();
    private double totalAmount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        prefs = PrefsManager.getInstance(this);
        cartDb = CartDatabase.getInstance(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadCartAndTotal();

        binding.btnPlaceOrder.setOnClickListener(v -> {
            if (validateDeliveryAddress()) initiatePayment();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    private void loadCartAndTotal() {
        cartDb.cartDao().getAllItems().observe(this, items -> {
            if (items == null) return;

            cartItems = items;
            totalAmount = 0;

            for (CartItem item : items)
                totalAmount += item.getTotalPrice();

            totalAmount += 350; // delivery fee

            binding.tvItemCount.setText(items.size() + " item(s)");
            binding.tvSubtotal.setText(String.format("LKR %.2f", totalAmount - 350));
            binding.tvDelivery.setText("LKR 350.00");
            binding.tvTotal.setText(String.format("LKR %.2f", totalAmount));
            binding.etDeliveryAddress.setText(prefs.getDeliveryAddress());
        });
    }


    private boolean validateDeliveryAddress() {
        String addr = binding.etDeliveryAddress.getText().toString().trim();
        if (addr.isEmpty()) {
            binding.tilDeliveryAddress.setError("Delivery address is required");
            return false;
        }
        binding.tilDeliveryAddress.setError(null);
        prefs.setDeliveryAddress(addr);
        return true;
    }


    private void initiatePayment() {
        String orderId = "ORD-" + System.currentTimeMillis();

        // Split name safely
        String fullName = prefs.getUserName();
        String[] parts = fullName.trim().split(" ", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : ".";

        // Build request
        InitRequest req = new InitRequest();
        req.setMerchantId("1228494");               // sandbox merchant ID
        req.setCurrency("LKR");
        req.setAmount(totalAmount);
        req.setOrderId(orderId);
        req.setItemsDescription("ShopEase Order");

        // Customer details
        req.getCustomer().setFirstName(firstName);
        req.getCustomer().setLastName(lastName);
        req.getCustomer().setEmail(prefs.getUserEmail());
        req.getCustomer().setPhone("+94771234567");

        // Billing address
        req.getCustomer().getAddress().setAddress(
                binding.etDeliveryAddress.getText().toString().trim());
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");

        // Delivery address
        req.getCustomer().getDeliveryAddress().setAddress(
                binding.etDeliveryAddress.getText().toString().trim());
        req.getCustomer().getDeliveryAddress().setCity("Colombo");
        req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");

        // Add cart items
        for (CartItem item : cartItems) {
            req.getItems().add(new Item(
                    null,
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice()
            ));
        }

        // Set sandbox
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

        // Launch PayHere payment screen
        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PAYHERE_REQUEST) return;
        if (data == null || !data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) return;

        PHResponse<StatusResponse> response =
                (PHResponse<StatusResponse>) data.getSerializableExtra(
                        PHConstants.INTENT_EXTRA_RESULT);

        if (resultCode == Activity.RESULT_OK) {
            if (response != null && response.isSuccess()) {
                // Payment successful
                saveOrderToFirestore(
                        response.getData().toString(),
                        Order.STATUS_CONFIRMED);
            } else {
                // Payment failed
                String msg = response != null ? response.toString() : "Payment failed";
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                saveOrderToFirestore("FAILED", Order.STATUS_CANCELLED);
            }

        } else if (resultCode == Activity.RESULT_CANCELED) {
            String msg = response != null ? response.toString() : "Payment cancelled";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }



    private void saveOrderToFirestore(String paymentId, String status) {
        String orderId = "ORD-" + System.currentTimeMillis();

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(prefs.getUserId());
        order.setStatus(status);
        order.setPaymentId(paymentId);
        order.setTotalAmount(totalAmount);
        order.setDeliveryAddress(
                binding.etDeliveryAddress.getText().toString().trim());
        order.setCreatedAt(System.currentTimeMillis());
        order.setItems(cartItems);

        db.collection("orders").document(orderId).set(order)
                .addOnSuccessListener(v -> {
                    // Clear cart on background thread
                    Executors.newSingleThreadExecutor().execute(
                            () -> cartDb.cartDao().clearCart());

                    // Show result message
                    String msg = status.equals(Order.STATUS_CONFIRMED)
                            ? "Order placed! 🎉"
                            : "Order was cancelled";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    // Send notification
                    NotificationHelper.showOrderNotification(this, orderId, status);

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save order: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}