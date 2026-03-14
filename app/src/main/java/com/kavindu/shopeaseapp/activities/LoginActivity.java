package com.kavindu.shopeaseapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.databinding.ActivityLoginBinding;
import com.kavindu.shopeaseapp.models.User;
import com.kavindu.shopeaseapp.utils.PrefsManager;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = PrefsManager.getInstance(this);

        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void loginUser() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            return;
        }

        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Logging in...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    String uid = result.getUser().getUid();
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                User user = doc.toObject(User.class);
                                if (user != null) {
                                    prefs.saveLoginState(uid, user.getName(), user.getEmail());
                                }
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    binding.btnLogin.setEnabled(true);
                    binding.btnLogin.setText("LOGIN");
                });
    }

    private void resetPassword() {
        String email = binding.etEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Enter email for reset");
            return;
        }
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(v -> Toast.makeText(this,
                        "Reset email sent!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this,
                        e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}