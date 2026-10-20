package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.data.model.EyeHospital;
import com.example.myapplication.data.repository.DiscoverHospitalRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class DiscoverFragment extends Fragment implements LocationListener {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private TextView tvSubtitle;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private HospitalAdapter adapter;
    private DiscoverHospitalRepository repository;
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedClient;
    private boolean isLockingLocation = false;

    // Runtime Permission Launcher: Prompts user immediately when navigating to Discover
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (Boolean.TRUE.equals(fineGranted) || Boolean.TRUE.equals(coarseGranted)) {
                    obtainLiveLocation();
                } else {
                    setLoading(false);
                    if (tvSubtitle != null) tvSubtitle.setText("Location permission denied");
                    if (tvEmptyState != null) {
                        tvEmptyState.setText("Location access is required to find eye clinics within 25 km.\nTap here to grant permission.");
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setOnClickListener(v -> requestLocationPermissions());
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_discover, container, false);

        swipeRefresh = root.findViewById(R.id.swipeRefreshDiscover);
        recyclerView = root.findViewById(R.id.recyclerDiscoverHospitals);
        tvSubtitle = root.findViewById(R.id.tvDiscoverSubtitle);
        tvEmptyState = root.findViewById(R.id.tvEmptyState);
        progressBar = root.findViewById(R.id.progressDiscover);

        adapter = new HospitalAdapter(requireContext());
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(adapter);
        }

        repository = new DiscoverHospitalRepository();
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        fusedClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeColors(0xFF0284C7);
            swipeRefresh.setOnRefreshListener(this::requestLocationPermissions);
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Trigger runtime permission check every time user opens or returns to Discover tab
        requestLocationPermissions();
    }

    private void requestLocationPermissions() {
        boolean hasFine = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasFine || hasCoarse) {
            obtainLiveLocation();
        } else {
            // Prompt the Android OS runtime permission dialog
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void obtainLiveLocation() {
        if (isLockingLocation) return;
        isLockingLocation = true;
        setLoading(true);

        if (tvSubtitle != null) {
            tvSubtitle.setText("Acquiring high-accuracy GPS position...");
        }

        boolean isGpsEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGpsEnabled && !isNetworkEnabled) {
            setLoading(false);
            isLockingLocation = false;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Location Services Disabled")
                    .setMessage("Please enable GPS/Location on your device to discover nearby eye care centers.")
                    .setPositiveButton("Enable Location", (d, w) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        // Fetch Live High-Accuracy Location via Fused Provider
        try {
            CancellationTokenSource cts = new CancellationTokenSource();
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            onRealLocationAcquired(location.getLatitude(), location.getLongitude());
                        } else {
                            // Fallback to one-shot hardware GPS/Network request
                            requestOneShotHardwareLocation();
                        }
                    })
                    .addOnFailureListener(e -> requestOneShotHardwareLocation());
        } catch (SecurityException se) {
            setLoading(false);
            isLockingLocation = false;
        }
    }

    private void requestOneShotHardwareLocation() {
        try {
            if (locationManager != null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            } else if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
            } else {
                setLoading(false);
                isLockingLocation = false;
                if (tvEmptyState != null) {
                    tvEmptyState.setText("Unable to acquire live GPS fix. Please check your location settings.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                }
            }
        } catch (SecurityException se) {
            setLoading(false);
            isLockingLocation = false;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        onRealLocationAcquired(location.getLatitude(), location.getLongitude());
    }

    private void onRealLocationAcquired(double lat, double lng) {
        isLockingLocation = false;
        if (tvSubtitle != null) {
            tvSubtitle.setText("Searching verified eye centers within 25 km...");
        }

        repository.fetchNearbyEyeHospitals(requireContext(), lat, lng, new DiscoverHospitalRepository.HospitalCallback() {
            @Override
            public void onSuccess(List<EyeHospital> hospitals, String detectedCity) {
                setLoading(false);
                if (tvSubtitle != null) {
                    tvSubtitle.setText("Showing verified eye centers near " + detectedCity + " (within 25 km)");
                }
                if (hospitals == null || hospitals.isEmpty()) {
                    if (tvEmptyState != null) {
                        tvEmptyState.setText("No eye care centers found within 25 km of your current location.");
                        tvEmptyState.setVisibility(View.VISIBLE);
                        tvEmptyState.setOnClickListener(null);
                    }
                    adapter.setHospitals(null);
                } else {
                    if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                    adapter.setHospitals(hospitals);
                }
            }

            @Override
            public void onError(String error) {
                setLoading(false);
                if (tvEmptyState != null) {
                    tvEmptyState.setText("Failed to load nearby eye centers. Pull down to refresh.");
                    tvEmptyState.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        if (swipeRefresh != null) swipeRefresh.setRefreshing(loading);
        if (progressBar != null) progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(@NonNull String provider) {}
    @Override public void onProviderDisabled(@NonNull String provider) {}
}