package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.db.AppDatabase;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class UserProfileBottomSheet extends BottomSheetDialogFragment {

    private TextView tvSheetRoleBadge, tvSheetUserName, tvSheetUserEmail, tvSheetAuthProvider;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_user_profile_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        tvSheetRoleBadge = view.findViewById(R.id.tvSheetRoleBadge);
        tvSheetUserName = view.findViewById(R.id.tvSheetUserName);
        tvSheetUserEmail = view.findViewById(R.id.tvSheetUserEmail);
        tvSheetAuthProvider = view.findViewById(R.id.tvSheetAuthProvider);

        TextView tvTotalLocalScans = view.findViewById(R.id.tvTotalLocalScans);
        TextView tvPendingQueueCount = view.findViewById(R.id.tvPendingQueueCount);

        MaterialButton btnEditProfile = view.findViewById(R.id.btnEditProfile);
        MaterialButton btnSignOut = view.findViewById(R.id.btnSignOut);

        refreshProfileUI(context);

        // Load Room DB scan metrics
        new Thread(() -> {
            try {
                int totalScans = AppDatabase.getInstance(context).scanRecordDao().getAllRecordsForSync().size();
                int pendingQueue = AppDatabase.getInstance(context).scanSyncDao().getPendingSyncItems().size();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvTotalLocalScans != null) tvTotalLocalScans.setText(totalScans + " Scans");
                        if (tvPendingQueueCount != null) tvPendingQueueCount.setText(pendingQueue + " Pending");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditProfileDialog(context));
        }

        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> {
                dismiss();
                SessionManager.clearSessionAndLogout(context);
                Toast.makeText(context, "Signed Out Successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }
    }

    private void refreshProfileUI(Context context) {
        if (tvSheetRoleBadge != null) tvSheetRoleBadge.setText(SessionManager.getUserRole(context));
        if (tvSheetUserName != null) tvSheetUserName.setText(SessionManager.getUserName(context));
        if (tvSheetUserEmail != null) tvSheetUserEmail.setText(SessionManager.getUserEmail(context));
        if (tvSheetAuthProvider != null) tvSheetAuthProvider.setText("Auth: " + SessionManager.getAuthProvider(context));
    }

    private void showEditProfileDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_profile);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextInputEditText etEditName = dialog.findViewById(R.id.etEditName);
        TextInputEditText etEditPhone = dialog.findViewById(R.id.etEditPhone);
        TextInputEditText etEditFacility = dialog.findViewById(R.id.etEditFacility);
        MaterialButton btnSaveProfile = dialog.findViewById(R.id.btnSaveProfile);
        Button btnCancelEdit = dialog.findViewById(R.id.btnCancelEdit);

        if (etEditName != null) etEditName.setText(SessionManager.getUserName(context));

        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> {
                String newName = etEditName != null && etEditName.getText() != null ? etEditName.getText().toString().trim() : "";
                if (!newName.isEmpty()) {
                    SessionManager.createGoogleSession(context, newName, SessionManager.getUserEmail(context), SessionManager.getUserRole(context));
                    Toast.makeText(context, R.string.toast_profile_updated, Toast.LENGTH_SHORT).show();
                    refreshProfileUI(context);
                    dialog.dismiss();
                }
            });
        }

        if (btnCancelEdit != null) {
            btnCancelEdit.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }
}