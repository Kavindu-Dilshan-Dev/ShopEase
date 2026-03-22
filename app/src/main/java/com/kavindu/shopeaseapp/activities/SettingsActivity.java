package com.kavindu.shopeaseapp.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.kavindu.shopeaseapp.databinding.ActivitySettingsBinding;
import com.kavindu.shopeaseapp.utils.FileStorageHelper;
import com.kavindu.shopeaseapp.utils.PrefsManager;

import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private PrefsManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }

        prefs = PrefsManager.getInstance(this);
        loadSettings();
        setupListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadSettings() {
        binding.switchNotifications.setChecked(prefs.isNotificationsEnabled());
        binding.switchDarkMode.setChecked(prefs.isDarkMode());
        binding.tvSortPref.setText("Sort: " + prefs.getSortPreference());

        // Load saved notes from internal storage
        String notes = FileStorageHelper.readFromInternalStorage(this, "user_notes.txt");
        if (notes != null) binding.etNotes.setText(notes);

        // Show storage info
        binding.tvStorageInfo.setText(FileStorageHelper.getStorageInfo(this));
    }

    private void setupListeners() {
        binding.switchNotifications.setOnCheckedChangeListener((btn, checked) -> {
            prefs.setNotifications(checked);
            android.widget.Toast.makeText(this,
                    "Notifications " + (checked ? "enabled" : "disabled"),
                    android.widget.Toast.LENGTH_SHORT).show();
        });

        binding.switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
            prefs.setDarkMode(checked);
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.btnSortDefault.setOnClickListener(v -> setSortPref("default"));
        binding.btnSortPrice.setOnClickListener(v ->   setSortPref("price_asc"));
        binding.btnSortRating.setOnClickListener(v ->  setSortPref("rating"));

        binding.btnSaveNotes.setOnClickListener(v -> {
            String notes = binding.etNotes.getText().toString();
            FileStorageHelper.writeToInternalStorage(this, "user_notes.txt", notes);
            android.widget.Toast.makeText(this, "Notes saved!", android.widget.Toast.LENGTH_SHORT).show();
        });

        binding.btnClearData.setOnClickListener(v -> {
            FileStorageHelper.deleteFile(this, "user_notes.txt");
            binding.etNotes.setText("");
            android.widget.Toast.makeText(this, "Data cleared", android.widget.Toast.LENGTH_SHORT).show();
        });

        binding.btnAbout.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("ShopEase v1.0")
                        .setMessage("A full-featured Android commerce app\nbuilt with Java + Firebase.\n\nDeveloped as part of Android coursework.")
                        .setPositiveButton("OK", null)
                        .show());
    }

    private void setSortPref(String pref) {
        prefs.setSortPreference(pref);
        binding.tvSortPref.setText("Sort: " + pref);
        android.widget.Toast.makeText(this, "Sort set to: " + pref,
                android.widget.Toast.LENGTH_SHORT).show();
    }
}