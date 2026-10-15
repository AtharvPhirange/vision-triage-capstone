package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class GuideFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup layoutGuideContainer = view.findViewById(R.id.layoutGuideContainer);
        if (layoutGuideContainer != null) {
            LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_animation_slide_up);
            layoutGuideContainer.setLayoutAnimation(animationController);
            layoutGuideContainer.startLayoutAnimation();
        }

        MaterialButton btnPracticeScan = view.findViewById(R.id.btnPracticeScan);
        if (btnPracticeScan != null) {
            btnPracticeScan.setOnClickListener(v -> {
                String activeId = PatientSessionManager.getActivePatientId(requireContext());
                Intent intent = new Intent(requireContext(), MainActivity.class);
                intent.putExtra("PATIENT_ID", activeId);
                startActivity(intent);
            });
        }
    }
}