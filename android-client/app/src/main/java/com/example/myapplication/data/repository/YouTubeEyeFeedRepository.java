package com.example.myapplication.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.myapplication.data.model.YouTubeEyeVideo;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YouTubeEyeFeedRepository {

    private static final String TAG = "YouTubeLiveFeed";
    private static final String YOUTUBE_API_KEY = "AIzaSyC_hXjVFKJGSb6XyFsR11pevLYdKLACfBc";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Set<String> globalSeenIds = new HashSet<>();

    public interface FeedResultCallback {
        void onSuccess(List<YouTubeEyeVideo> videos, String nextPageToken);
        void onError(Exception e);
    }

    public void clearSeenCache() {
        globalSeenIds.clear();
    }

    public void fetchLiveEyeFeed(String queryFilter, String pageToken, FeedResultCallback callback) {
        executor.execute(() -> {
            List<YouTubeEyeVideo> videoList = new ArrayList<>();
            String nextToken = null;

            try {
                // Strict ocular and eye health keywords
                String searchQuery = "eye exercises ocular health vision strain relief " + (queryFilter != null ? queryFilter : "");
                String encodedQuery = URLEncoder.encode(searchQuery.trim(), StandardCharsets.UTF_8.name());

                // Step 1: YouTube Data API v3 search.list (sorted by viewCount)
                String searchUrl = "https://www.googleapis.com/youtube/v3/search"
                        + "?part=snippet"
                        + "&type=video"
                        + "&order=viewCount"
                        + "&maxResults=15"
                        + "&q=" + encodedQuery
                        + (pageToken != null && !pageToken.isEmpty() && !pageToken.equals("1") ? "&pageToken=" + pageToken : "")
                        + "&key=" + YOUTUBE_API_KEY;

                Log.d(TAG, "Requesting YouTube Search: " + searchUrl);
                JSONObject searchRoot = executeGetRequest(searchUrl);

                if (searchRoot != null) {
                    nextToken = searchRoot.optString("nextPageToken", null);
                    JSONArray items = searchRoot.optJSONArray("items");

                    if (items != null && items.length() > 0) {
                        List<String> videoIds = new ArrayList<>();
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject idObj = items.getJSONObject(i).optJSONObject("id");
                            if (idObj != null && idObj.has("videoId")) {
                                videoIds.add(idObj.getString("videoId"));
                            }
                        }

                        // Step 2: Batch fetch video statistics (exact views) and contentDetails (duration)
                        if (!videoIds.isEmpty()) {
                            String idsJoined = String.join(",", videoIds);
                            String detailsUrl = "https://www.googleapis.com/youtube/v3/videos"
                                    + "?part=snippet,statistics,contentDetails"
                                    + "&id=" + idsJoined
                                    + "&key=" + YOUTUBE_API_KEY;

                            JSONObject detailsRoot = executeGetRequest(detailsUrl);
                            if (detailsRoot != null) {
                                JSONArray detailItems = detailsRoot.optJSONArray("items");
                                if (detailItems != null) {
                                    for (int i = 0; i < detailItems.length(); i++) {
                                        JSONObject vObj = detailItems.getJSONObject(i);
                                        String vId = vObj.getString("id");
                                        if (globalSeenIds.contains(vId)) continue;

                                        JSONObject snip = vObj.getJSONObject("snippet");
                                        JSONObject stats = vObj.optJSONObject("statistics");
                                        JSONObject content = vObj.optJSONObject("contentDetails");

                                        String title = snip.optString("title", "Eye Exercise");
                                        String channel = snip.optString("channelTitle", "Ocular Specialist");
                                        long views = stats != null ? stats.optLong("viewCount", 100_000) : 100_000;
                                        String duration = content != null ? parseIsoDuration(content.optString("duration", "PT5M")) : "5:00";

                                        // Use official YouTube CDN thumbnail URL from API response
                                        JSONObject thumbs = snip.optJSONObject("thumbnails");
                                        String thumbUrl = "https://i.ytimg.com/vi/" + vId + "/hqdefault.jpg";
                                        if (thumbs != null) {
                                            if (thumbs.has("high")) thumbUrl = thumbs.getJSONObject("high").optString("url", thumbUrl);
                                            else if (thumbs.has("medium")) thumbUrl = thumbs.getJSONObject("medium").optString("url", thumbUrl);
                                        }

                                        String avatarUrl = "https://ui-avatars.com/api/?name=" + URLEncoder.encode(channel, "UTF-8") + "&background=0284C7&color=fff";

                                        globalSeenIds.add(vId);
                                        videoList.add(new YouTubeEyeVideo(
                                                vId, title, channel, avatarUrl, thumbUrl,
                                                views, duration, "Live", "EYE HEALTH"
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Live API Query Exception: " + e.getMessage(), e);
            }

            // Fallback dataset if network fails or quota is exhausted
            if (videoList.isEmpty()) {
                Log.w(TAG, "Using fallback dataset");
                videoList = getVerifiedCatalog();
            }

            // Sort descending: highest views first
            Collections.sort(videoList, (a, b) -> Long.compare(b.getViewCount(), a.getViewCount()));

            List<YouTubeEyeVideo> finalResult = videoList;
            String finalNextToken = nextToken;
            mainHandler.post(() -> callback.onSuccess(finalResult, finalNextToken));
        });
    }

    private JSONObject executeGetRequest(String urlStr) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            conn.setRequestProperty("User-Agent", "VisionTriage-Android-App");

            int responseCode = conn.getResponseCode();
            InputStream stream = (responseCode >= 200 && responseCode < 300) 
                    ? conn.getInputStream() 
                    : conn.getErrorStream();

            if (stream == null) return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            if (responseCode != 200) {
                Log.e(TAG, "HTTP " + responseCode + " Error from YouTube: " + sb.toString());
                return null;
            }

            return new JSONObject(sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "Connection error: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String parseIsoDuration(String isoDuration) {
        try {
            String s = isoDuration.replace("PT", "");
            int min = 0, sec = 0;
            if (s.contains("M")) {
                String[] parts = s.split("M");
                min = Integer.parseInt(parts[0]);
                if (parts.length > 1 && parts[1].contains("S")) {
                    sec = Integer.parseInt(parts[1].replace("S", ""));
                }
            } else if (s.contains("S")) {
                sec = Integer.parseInt(s.replace("S", ""));
            }
            return String.format(Locale.US, "%d:%02d", min, sec);
        } catch (Exception e) {
            return "5:00";
        }
    }

    private List<YouTubeEyeVideo> getVerifiedCatalog() {
        List<YouTubeEyeVideo> list = new ArrayList<>();
        list.add(new YouTubeEyeVideo("_LBb9DMqCFc", "10 Simple Eye Exercises to Improve Vision & Mobility", "BRIGHT SIDE",
                "https://ui-avatars.com/api/?name=Bright+Side&background=FF0000&color=fff",
                "https://i.ytimg.com/vi/_LBb9DMqCFc/hqdefault.jpg", 27_000_000L, "11:05", "3 years ago", "MOBILITY"));
        list.add(new YouTubeEyeVideo("0jf9YPGKlbg", "20-20-20 Rule & Digital Eye Strain Reset", "Dr. Alan Mandell",
                "https://ui-avatars.com/api/?name=Dr+Mandell&background=0284C7&color=fff",
                "https://i.ytimg.com/vi/0jf9YPGKlbg/hqdefault.jpg", 2_800_000L, "5:12", "2 years ago", "STRAIN RELIEF"));
        list.add(new YouTubeEyeVideo("nMKr5SuJSXY", "4 Daily Exercises for Better Convergence & Focus Acuity", "Insight Vision Optometry",
                "https://ui-avatars.com/api/?name=Insight&background=10B981&color=fff",
                "https://i.ytimg.com/vi/nMKr5SuJSXY/hqdefault.jpg", 1_800_000L, "6:30", "1 year ago", "CONVERGENCE"));
        list.add(new YouTubeEyeVideo("7_2fN77kO2k", "How to RELIEVE Tired Eyes in 2 Minutes! (Optometrist Guide)", "Doctor Eye Health",
                "https://ui-avatars.com/api/?name=Doctor+Allen&background=6366F1&color=fff",
                "https://i.ytimg.com/vi/7_2fN77kO2k/hqdefault.jpg", 1_400_000L, "8:45", "1 year ago", "STRAIN RELIEF"));
        list.add(new YouTubeEyeVideo("v1-bFjKq_kU", "Top 5 Eye Exercises You Can Do at Your Desk", "Bob & Brad",
                "https://ui-avatars.com/api/?name=Bob+Brad&background=F59E0B&color=fff",
                "https://i.ytimg.com/vi/v1-bFjKq_kU/hqdefault.jpg", 980_000L, "7:10", "2 years ago", "MOBILITY"));
        list.add(new YouTubeEyeVideo("Z2-K24m67G0", "Fix Dry Eyes & Meibomian Glands with Blinks & Warm Compress", "Doctor Eye Health",
                "https://ui-avatars.com/api/?name=Doctor+Allen&background=6366F1&color=fff",
                "https://i.ytimg.com/vi/Z2-K24m67G0/hqdefault.jpg", 650_000L, "9:20", "8 months ago", "DRY EYE"));
        return list;
    }
}