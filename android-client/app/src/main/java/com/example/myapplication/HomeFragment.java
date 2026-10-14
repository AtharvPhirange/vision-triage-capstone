package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View cardPatientPortal = view.findViewById(R.id.cardPatientPortal);
        Button btnPatientPortal = view.findViewById(R.id.btnPatientPortal);
        Button btnPatientHistory = view.findViewById(R.id.btnPatientHistory);
        Button btnBookConsultation = view.findViewById(R.id.btnBookConsultation);

        View cardTechnicianPortal = view.findViewById(R.id.cardTechnicianPortal);
        Button btnInitiateScan = view.findViewById(R.id.btnInitiateScan);
        Button btnPatientIntake = view.findViewById(R.id.btnPatientIntake);
        Button btnDeviceCalibration = view.findViewById(R.id.btnDeviceCalibration);
        Button btnSyncQueue = view.findViewById(R.id.btnSyncQueue);

        View cardConsultantPortal = view.findViewById(R.id.cardConsultantPortal);
        Button btnReviewQueue = view.findViewById(R.id.btnReviewQueue);
        Button btnHighRiskCases = view.findViewById(R.id.btnHighRiskCases);
        Button btnVerifyReports = view.findViewById(R.id.btnVerifyReports);

        // Card 1 Listeners
        View.OnClickListener accessPatientPortalListener = v -> startActivity(new Intent(requireContext(), PatientPortalActivity.class));
        if (cardPatientPortal != null) cardPatientPortal.setOnClickListener(accessPatientPortalListener);
        if (btnPatientPortal != null) btnPatientPortal.setOnClickListener(accessPatientPortalListener);

        if (btnPatientHistory != null) {
            btnPatientHistory.setOnClickListener(v -> {
                String activeId = PatientSessionManager.getActivePatientId(requireContext());
                Intent intent = new Intent(requireContext(), ScanHistoryActivity.class);
                intent.putExtra("PATIENT_ID", activeId);
                startActivity(intent);
            });
        }

        if (btnBookConsultation != null) {
            btnBookConsultation.setOnClickListener(v -> Toast.makeText(requireContext(), "Opening Consultation Booking...", Toast.LENGTH_SHORT).show());
        }

        // Card 2 Listeners
        View.OnClickListener initiateScanListener = v -> {
            String activeId = PatientSessionManager.getActivePatientId(requireContext());
            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.putExtra("PATIENT_ID", activeId);
            startActivity(intent);
        };

        if (cardTechnicianPortal != null) {
            cardTechnicianPortal.setOnClickListener(v -> startActivity(new Intent(requireContext(), TechnicianLoginActivity.class)));
        }

        if (btnInitiateScan != null) btnInitiateScan.setOnClickListener(initiateScanListener);

        if (btnPatientIntake != null) {
            btnPatientIntake.setOnClickListener(v -> startActivity(new Intent(requireContext(), TechnicianIntakeActivity.class)));
        }

        if (btnDeviceCalibration != null) {
            btnDeviceCalibration.setOnClickListener(v -> startActivity(new Intent(requireContext(), DevicePairingActivity.class)));
        }

        if (btnSyncQueue != null) {
            btnSyncQueue.setOnClickListener(v -> startActivity(new Intent(requireContext(), SyncQueueActivity.class)));
        }

        // Card 3 Listeners
        View.OnClickListener consultantQueueListener = v -> startActivity(new Intent(requireContext(), ConsultantPriorityQueueActivity.class));
        if (cardConsultantPortal != null) cardConsultantPortal.setOnClickListener(consultantQueueListener);
        if (btnReviewQueue != null) btnReviewQueue.setOnClickListener(consultantQueueListener);
        if (btnHighRiskCases != null) btnHighRiskCases.setOnClickListener(v -> startActivity(new Intent(requireContext(), ConsultantPriorityQueueActivity.class)));

        if (btnVerifyReports != null) {
            btnVerifyReports.setOnClickListener(v -> Toast.makeText(requireContext(), "Opening Diagnostic Report Verifier...", Toast.LENGTH_SHORT).show());
        }
    }
}