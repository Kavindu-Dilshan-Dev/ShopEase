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

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kavindu.shopeaseapp.databinding.ActivityCameraBinding;
import com.kavindu.shopeaseapp.utils.PrefsManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

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
        binding.btnUpload.setText("Uploading…");

        String uid      = PrefsManager.getInstance(this).getUserId();
        String fileName = "profile_" + uid + "_" +
                System.currentTimeMillis();

        // ✅ Upload to Cloudinary
        MediaManager.get()
                .upload(photoUri)
                .option("public_id", "shopease/profiles/" + fileName)
                .option("folder", "shopease/profiles")
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        runOnUiThread(() ->
                                binding.progressBar.setProgress(0));
                    }

                    @Override
                    public void onProgress(String requestId,
                                           long bytes, long totalBytes) {
                        int progress = (int)
                                ((bytes * 100) / totalBytes);
                        runOnUiThread(() ->
                                binding.progressBar.setProgress(progress));
                    }

                    @Override
                    public void onSuccess(String requestId,
                                          Map resultData) {
                        // Get the secure URL
                        String imageUrl = (String)
                                resultData.get("secure_url");

                        runOnUiThread(() -> {
                            binding.progressBar.setVisibility(
                                    android.view.View.GONE);
                            binding.btnUpload.setText("Upload");
                            binding.btnUpload.setEnabled(true);

                            Toast.makeText(CameraActivity.this,
                                    "Photo uploaded! ✅",
                                    Toast.LENGTH_SHORT).show();

                            // ✅ Save URL to Firestore
                            com.google.firebase.firestore
                                    .FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(uid)
                                    .update("profileImageUrl", imageUrl)
                                    .addOnSuccessListener(v ->
                                            Toast.makeText(CameraActivity.this,
                                                    "Profile photo updated!",
                                                    Toast.LENGTH_SHORT).show());
                        });
                    }

                    @Override
                    public void onError(String requestId,
                                        ErrorInfo error) {
                        runOnUiThread(() -> {
                            binding.progressBar.setVisibility(
                                    android.view.View.GONE);
                            binding.btnUpload.setText("Upload");
                            binding.btnUpload.setEnabled(true);
                            Toast.makeText(CameraActivity.this,
                                    "Upload failed: " + error.getDescription(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onReschedule(String requestId,
                                             ErrorInfo error) {}
                })
                .dispatch();
    }
}