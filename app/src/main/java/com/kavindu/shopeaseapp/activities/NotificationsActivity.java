package com.kavindu.shopeaseapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.kavindu.shopeaseapp.adapters.NotificationAdapter;
import com.kavindu.shopeaseapp.databinding.ActivityNotificationsBinding;
import com.kavindu.shopeaseapp.models.NotificationModel;
import com.kavindu.shopeaseapp.utils.PrefsManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private FirebaseFirestore db;
    private PrefsManager prefs;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList = new ArrayList<>();

    // ─────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        prefs = PrefsManager.getInstance(this);

        setupToolbar();
        setupRecyclerView();
        loadNotifications();

        binding.btnClearAll.setOnClickListener(v -> clearAllNotifications());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ─────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, notificationList);
        binding.rvNotifications.setLayoutManager(
                new LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(adapter);
    }

    // ─────────────────────────────────────────────
    // Load Notifications from Firestore
    // ─────────────────────────────────────────────

    private void loadNotifications() {
        binding.progressBar.setVisibility(View.VISIBLE);
        String uid = prefs.getUserId();

        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    binding.progressBar.setVisibility(View.GONE);

                    if (e != null || snapshots == null) {
                        showEmpty();
                        return;
                    }

                    notificationList.clear();

                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        NotificationModel notif =
                                doc.toObject(NotificationModel.class);
                        if (notif != null) {
                            notif.setId(doc.getId());
                            notificationList.add(notif);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (notificationList.isEmpty()) {
                        showEmpty();
                    } else {
                        binding.tvEmpty.setVisibility(View.GONE);
                        binding.rvNotifications.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ─────────────────────────────────────────────
    // Clear All
    // ─────────────────────────────────────────────

    private void clearAllNotifications() {
        if (notificationList.isEmpty()) {
            Toast.makeText(this,
                    "No notifications to clear", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = prefs.getUserId();

        db.collection("notifications")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        doc.getReference().delete();
                    }
                    notificationList.clear();
                    adapter.notifyDataSetChanged();
                    showEmpty();
                    Toast.makeText(this,
                            "All notifications cleared",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to clear: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // ─────────────────────────────────────────────
    // Empty State
    // ─────────────────────────────────────────────

    private void showEmpty() {
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.rvNotifications.setVisibility(View.GONE);
    }
}
