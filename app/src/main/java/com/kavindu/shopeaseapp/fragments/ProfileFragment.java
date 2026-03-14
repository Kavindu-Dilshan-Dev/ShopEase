package com.kavindu.shopeaseapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kavindu.shopeaseapp.R;
import com.kavindu.shopeaseapp.activities.CameraActivity;
import com.kavindu.shopeaseapp.activities.LoginActivity;
import com.kavindu.shopeaseapp.activities.MapActivity;
import com.kavindu.shopeaseapp.activities.MultimediaActivity;
import com.kavindu.shopeaseapp.activities.OrdersActivity;
import com.kavindu.shopeaseapp.activities.ProfileActivity;
import com.kavindu.shopeaseapp.activities.SensorsActivity;
import com.kavindu.shopeaseapp.activities.SettingsActivity;
import com.kavindu.shopeaseapp.databinding.FragmentProfileBinding;
import com.kavindu.shopeaseapp.models.User;
import com.kavindu.shopeaseapp.utils.PrefsManager;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseFirestore db;
    private PrefsManager prefs;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db    = FirebaseFirestore.getInstance();
        prefs = PrefsManager.getInstance(requireContext());

        loadUserProfile();
        setupClickListeners();
    }

    private void loadUserProfile() {
        String uid = prefs.getUserId();
        if (uid.isEmpty()) return;

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user == null) return;

                    binding.tvUserName.setText(user.getName());
                    binding.tvUserEmail.setText(user.getEmail());
                    binding.tvUserPhone.setText(user.getPhone() != null ? user.getPhone() : "Not set");

                    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                        Glide.with(this)
                                .load(user.getProfileImageUrl())
                                .circleCrop()
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(binding.ivProfileImage);
                    }
                });
    }

    private void setupClickListeners() {
        binding.ivProfileImage.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CameraActivity.class)));

        binding.btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ProfileActivity.class)));

        binding.cardOrders.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), OrdersActivity.class)));

        binding.cardMap.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MapActivity.class)));

        binding.cardCamera.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CameraActivity.class)));

        binding.cardMultimedia.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MultimediaActivity.class)));

        binding.cardSensors.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SensorsActivity.class)));

        binding.cardSettings.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            prefs.logout();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}