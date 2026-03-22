package com.kavindu.shopeaseapp.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kavindu.shopeaseapp.databinding.ActivityCameraBinding;
import com.kavindu.shopeaseapp.utils.PrefsManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {

    private ActivityCameraBinding binding;
    private Uri photoUri;
    private FirebaseStorage storage;
    private String currentPhotoPath;

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoUri != null) {
                    binding.ivCaptured.setImageURI(photoUri);
                    binding.btnUpload.setEnabled(true);
                }
            });

    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    photoUri = uri;
                    binding.ivCaptured.setImageURI(uri);
                    binding.btnUpload.setEnabled(true);
                }
            });

    private final ActivityResultLauncher<String[]> permLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (Boolean.TRUE.equals(result.get(Manifest.permission.CAMERA)))
                    launchCamera();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Camera / Gallery");
        }
        storage = FirebaseStorage.getInstance();

        binding.btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                permLauncher.launch(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE});
            }
        });

        binding.btnGallery.setOnClickListener(v ->
                galleryLauncher.launch("image/*"));

        binding.btnUpload.setOnClickListener(v -> uploadToFirebase());

        binding.btnUpload.setEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(photoUri);
        } catch (IOException e) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("SHOPEASE_" + stamp, ".jpg", dir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadToFirebase() {
        if (photoUri == null) return;
        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        binding.btnUpload.setEnabled(false);

        String uid = PrefsManager.getInstance(this).getUserId();
        String fileName = "profile_" + uid + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference ref = storage.getReference("profile_images/" + fileName);

        ref.putFile(photoUri)
                .addOnProgressListener(snap -> {
                    double progress = (100.0 * snap.getBytesTransferred()) / snap.getTotalByteCount();
                    binding.progressBar.setProgress((int) progress);
                })
                .addOnSuccessListener(snap -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        binding.progressBar.setVisibility(android.view.View.GONE);
                        Toast.makeText(this, "Photo uploaded!", Toast.LENGTH_SHORT).show();
                        // Update profile image URL in Firestore
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(uid)
                                .update("profileImageUrl", uri.toString());
                        binding.btnUpload.setEnabled(true);
                    });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    binding.btnUpload.setEnabled(true);
                });
    }
}