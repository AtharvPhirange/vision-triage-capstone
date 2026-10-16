package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionsActivity extends BaseActivity {

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Toast.makeText(this, "Permissions updated", Toast.LENGTH_SHORT).show();
                proceedToHome();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Check if permissions are already granted
        if (hasAllPermissions()) {
            proceedToHome();
            return;
        }

        MaterialButton btnGrantPermissions = findViewById(R.id.btnGrantPermissions);
        if (btnGrantPermissions != null) {
            btnGrantPermissions.setOnClickListener(v -> requestPermissions());
        }
    }

    private boolean hasAllPermissions() {
        boolean camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean location = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return camera && location;
    }

    private void requestPermissions() {
        List<String> perms = new ArrayList<>(Arrays.asList(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.READ_MEDIA_IMAGES);
            perms.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        permissionLauncher.launch(perms.toArray(new String[0]));
    }

    private void proceedToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}