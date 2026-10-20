package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.data.model.YouTubeEyeVideo;
import com.example.myapplication.data.repository.YouTubeEyeFeedRepository;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class FitnessFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private YouTubeFeedAdapter adapter;
    private YouTubeEyeFeedRepository repository;
    private String currentFilter = "";
    private String nextToken = null;
    private boolean isLoading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fitness, container, false);

        swipeRefresh = root.findViewById(R.id.swipeRefreshFitness);
        RecyclerView recyclerView = root.findViewById(R.id.recyclerFitnessFeed);
        ChipGroup chipGroup = root.findViewById(R.id.chipGroupFilters);

        adapter = new YouTubeFeedAdapter(requireContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        if (recyclerView != null) {
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            // Infinite Scroll Listener
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                    if (dy > 0 && !isLoading && nextToken != null) {
                        int visibleCount = layoutManager.getChildCount();
                        int totalCount = layoutManager.getItemCount();
                        int firstVisible = layoutManager.findFirstVisibleItemPosition();

                        if ((visibleCount + firstVisible) >= totalCount - 3) {
                            loadPage(false);
                        }
                    }
                }
            });
        }

        repository = new YouTubeEyeFeedRepository();

        // Filter Chips selection
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) return;
                int id = checkedIds.get(0);
                if (id == R.id.chipAll) currentFilter = "";
                else if (id == R.id.chipStrain) currentFilter = "20-20-20 rule digital eye strain";
                else if (id == R.id.chipMobility) currentFilter = "eye mobility exercises eye yoga";
                else if (id == R.id.chipDryEye) currentFilter = "dry eye relief palming";

                loadPage(true);
            });
        }

        if (swipeRefresh != null) {
            swipeRefresh.setColorSchemeColors(0xFFDC2626, 0xFF0284C7);
            swipeRefresh.setOnRefreshListener(() -> loadPage(true));
        }

        loadPage(true);
        return root;
    }

    private void loadPage(boolean refresh) {
        if (isLoading) return;
        isLoading = true;

        if (refresh) {
            nextToken = null;
            if (swipeRefresh != null) swipeRefresh.setRefreshing(true);
        }

        repository.fetchLiveEyeFeed(currentFilter, nextToken, new YouTubeEyeFeedRepository.FeedResultCallback() {
            @Override
            public void onSuccess(List<YouTubeEyeVideo> videos, String token) {
                nextToken = token;
                if (refresh) {
                    adapter.setVideos(videos);
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                } else {
                    adapter.addVideos(videos);
                }
                isLoading = false;
            }

            @Override
            public void onError(Exception e) {
                if (refresh && swipeRefresh != null) swipeRefresh.setRefreshing(false);
                isLoading = false;
            }
        });
    }
}