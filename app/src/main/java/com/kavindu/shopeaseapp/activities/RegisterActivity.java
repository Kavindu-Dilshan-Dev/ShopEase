package com.kavindu.shopeaseapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.databinding.ActivityRegisterBinding;
import com.kavindu.shopeaseapp.models.User;
import com.kavindu.shopeaseapp.utils.PrefsManager;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        binding.btnRegister.setOnClickListener(v -> registerUser());
        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name     = binding.etName.getText().toString().trim();
        String email    = binding.etEmail.getText().toString().trim();
        String phone    = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirm  = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirm)) {
            binding.tilConfirmPassword.setError("Passwords do not match"); return;
        }
        if (password.length() < 6) {
            binding.tilPassword.setError("Min 6 characters"); return;
        }

        binding.btnRegister.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid  = result.getUser().getUid();
                    User user   = new User(uid, name, email);
                    user.setPhone(phone);

                    db.collection("users").document(uid).set(user)
                            .addOnSuccessListener(v -> {
                                PrefsManager.getInstance(this).saveLoginState(uid, name, email);
                                startActivity(new Intent(this, MainActivity.class));
                                finishAffinity();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnRegister.setEnabled(true);
                });
    }
}