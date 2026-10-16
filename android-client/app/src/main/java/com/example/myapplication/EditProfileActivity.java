package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends BaseActivity {

    private ImageView ivEditProfileAvatar;
    private TextInputEditText etEditName, etEditPhone, etEditEmail, etEditFacility, etEditDepartment, etEditStaffId, etEditEmergencyPhone, etEditLocation;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (Exception ignored) {}
                SessionManager.setUserPhotoUri(this, uri.toString());
                Toast.makeText(this, R.string.toast_avatar_updated, Toast.LENGTH_SHORT).show();
                loadProfileAvatar();
            }
        });

        View btnBackEditProfile = findViewById(R.id.btnBackEditProfile);
        TextView btnTopSaveProfile = findViewById(R.id.btnTopSaveProfile);
        View btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        ivEditProfileAvatar = findViewById(R.id.ivEditProfileAvatar);

        etEditName = findViewById(R.id.etEditName);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditEmail = findViewById(R.id.etEditEmail);
        etEditFacility = findViewById(R.id.etEditFacility);
        etEditDepartment = findViewById(R.id.etEditDepartment);
        etEditStaffId = findViewById(R.id.etEditStaffId);
        etEditEmergencyPhone = findViewById(R.id.etEditEmergencyPhone);
        etEditLocation = findViewById(R.id.etEditLocation);

        View btnSaveProfile = findViewById(R.id.btnSaveProfile);

        loadCurrentProfileData();

        if (btnBackEditProfile != null) {
            btnBackEditProfile.setOnClickListener(v -> finish());
        }

        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> showAvatarSourceDialog());
        }

        View.OnClickListener saveListener = v -> saveProfileData();
        if (btnTopSaveProfile != null) btnTopSaveProfile.setOnClickListener(saveListener);
        if (btnSaveProfile != null) btnSaveProfile.setOnClickListener(saveListener);
    }

    private void loadCurrentProfileData() {
        if (etEditName != null) etEditName.setText(SessionManager.getUserName(this));
        if (etEditEmail != null) etEditEmail.setText(SessionManager.getUserEmail(this));

        loadProfileAvatar();
    }

    private void loadProfileAvatar() {
        String photoUri = SessionManager.getUserPhotoUri(this);
        if (ivEditProfileAvatar != null) {
            if (photoUri != null && !photoUri.isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(photoUri))
                        .placeholder(R.drawable.ic_account_circle)
                        .error(R.drawable.ic_account_circle)
                        .circleCrop()
                        .into(ivEditProfileAvatar);
            } else {
                ivEditProfileAvatar.setImageResource(R.drawable.ic_account_circle);
            }
        }
    }

    private void showAvatarSourceDialog() {
        String[] options = {
                getString(R.string.opt_choose_gallery),
                getString(R.string.opt_use_google),
                getString(R.string.opt_reset_avatar)
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_select_avatar_title)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    } else if (which == 1) {
                        String googleUrl = SessionManager.getGooglePhotoUrl(this);
                        if (googleUrl != null && !googleUrl.isEmpty()) {
                            SessionManager.setUserPhotoUri(this, googleUrl);
                            loadProfileAvatar();
                            Toast.makeText(this, R.string.toast_google_avatar_applied, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.toast_no_google_avatar, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        SessionManager.setUserPhotoUri(this, null);
                        loadProfileAvatar();
                        Toast.makeText(this, R.string.toast_avatar_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void saveProfileData() {
        String newName = etEditName != null && etEditName.getText() != null ? etEditName.getText().toString().trim() : "";
        if (!newName.isEmpty()) {
            SessionManager.createGoogleSession(this, newName, SessionManager.getUserEmail(this), SessionManager.getUserRole(this));
            Toast.makeText(this, R.string.toast_profile_updated, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.error_required_field, Toast.LENGTH_SHORT).show();
        }
    }
}