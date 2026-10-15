package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.myapplication.db.AppDatabase;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

public class UserProfileBottomSheet extends BottomSheetDialogFragment {

    private TextView tvSheetRoleBadge, tvSheetUserName, tvSheetUserEmail, tvSheetAuthProvider;
    private ImageView imgSheetAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_user_profile_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = requireContext();

        imgSheetAvatar = view.findViewById(R.id.imgSheetAvatar);
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
            btnEditProfile.setOnClickListener(v -> {
                dismiss();
                startActivity(new Intent(context, EditProfileActivity.class));
            });
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

        String photoUri = SessionManager.getUserPhotoUri(context);
        if (imgSheetAvatar != null && photoUri != null) {
            Glide.with(context)
                    .load(Uri.parse(photoUri))
                    .placeholder(R.drawable.ic_account_circle)
                    .error(R.drawable.ic_account_circle)
                    .circleCrop()
                    .into(imgSheetAvatar);
        } else if (imgSheetAvatar != null) {
            imgSheetAvatar.setImageResource(R.drawable.ic_account_circle);
        }
    }
}