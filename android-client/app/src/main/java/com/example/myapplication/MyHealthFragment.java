package com.example.myapplication;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;
import java.util.Random;

public class MyHealthFragment extends Fragment {

    private TextView tvHealthTipDetail;
    private final Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_health, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvHealthTipDetail = view.findViewById(R.id.tvHealthTipDetail);
        MaterialButton btnOpenFullInsight = view.findViewById(R.id.btnOpenFullInsight);
        MaterialButton btnNextTip = view.findViewById(R.id.btnNextTip);

        showRandomTip();

        if (btnOpenFullInsight != null) {
            btnOpenFullInsight.setOnClickListener(v -> openFullInsightDialog());
        }

        if (btnNextTip != null) {
            btnNextTip.setOnClickListener(v -> animateNextTip());
        }
    }

    private void showRandomTip() {
        if (tvHealthTipDetail == null) return;
        String[] tips = getResources().getStringArray(R.array.clinical_eye_tips_detailed);
        if (tips.length > 0) {
            tvHealthTipDetail.setText(tips[random.nextInt(tips.length)]);
        } else {
            String[] fallbackTips = getResources().getStringArray(R.array.clinical_eye_tips);
            if (fallbackTips.length > 0) {
                tvHealthTipDetail.setText(fallbackTips[random.nextInt(fallbackTips.length)]);
            }
        }
    }

    private void animateNextTip() {
        if (tvHealthTipDetail == null) return;
        tvHealthTipDetail.animate().alpha(0f).setDuration(200).withEndAction(() -> {
            showRandomTip();
            tvHealthTipDetail.animate().alpha(1f).setDuration(200);
        });
    }

    private void openFullInsightDialog() {
        Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_health_insight);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvMechanismDetail = dialog.findViewById(R.id.tvMechanismDetail);
        if (tvMechanismDetail != null && tvHealthTipDetail != null) {
            tvMechanismDetail.setText(tvHealthTipDetail.getText());
        }

        View btnCloseInsight = dialog.findViewById(R.id.btnCloseInsight);
        if (btnCloseInsight != null) {
            btnCloseInsight.setOnClickListener(v -> dialog.dismiss());
        }

        MaterialButton btnCompleteRoutine = dialog.findViewById(R.id.btnCompleteRoutine);
        if (btnCompleteRoutine != null) {
            btnCompleteRoutine.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "🎉 Routine Completed! +50 Wellness Points Added", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}