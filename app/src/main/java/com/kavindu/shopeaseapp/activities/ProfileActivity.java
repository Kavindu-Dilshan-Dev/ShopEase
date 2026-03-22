package com.kavindu.shopeaseapp.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.databinding.ActivityProfileBinding;
import com.kavindu.shopeaseapp.models.User;
import com.kavindu.shopeaseapp.utils.PrefsManager;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private FirebaseFirestore db;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }


        db = FirebaseFirestore.getInstance();
        prefs = PrefsManager.getInstance(this);

        loadProfile();

        binding.btnSave.setOnClickListener(v -> saveProfile());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadProfile() {
        db.collection("users").document(prefs.getUserId()).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user == null) return;
                    binding.etName.setText(user.getName());
                    binding.etPhone.setText(user.getPhone());
                    binding.etAddress.setText(user.getAddress());
                });
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError("Name required");
            return;
        }

        binding.btnSave.setEnabled(false);

        db.collection("users").document(prefs.getUserId())
                .update("name", name, "phone", phone, "address", address)
                .addOnSuccessListener(v -> {
                    prefs.saveLoginState(prefs.getUserId(), name, prefs.getUserEmail());
                    if (!address.isEmpty()) prefs.setDeliveryAddress(address);
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    binding.btnSave.setEnabled(true);
                });
    }
}