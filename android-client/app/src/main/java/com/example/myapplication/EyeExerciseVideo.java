package com.example.myapplication;

import java.util.Locale;

public class EyeExerciseVideo {
    private String videoId;
    private String title;
    private String channelTitle;
    private String description;
    private String thumbnailUrl;
    private long viewCount;
    private String duration;
    private String categoryTag;

    public EyeExerciseVideo(String videoId, String title, String channelTitle,
                            String description, String thumbnailUrl, long viewCount,
                            String duration, String categoryTag) {
        this.videoId = videoId;
        this.title = title;
        this.channelTitle = channelTitle;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.viewCount = viewCount;
        this.duration = duration;
        this.categoryTag = categoryTag;
    }

    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
    public String getChannelTitle() { return channelTitle; }
    public String getDescription() { return description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public long getViewCount() { return viewCount; }
    public String getDuration() { return duration; }
    public String getCategoryTag() { return categoryTag; }

    public String getFormattedViewCount() {
        if (viewCount >= 1_000_000) {
            return String.format(Locale.US, "%.1fM views", viewCount / 1_000_000.0)
                    .replace(".0M", "M");
        } else if (viewCount >= 1_000) {
            return String.format(Locale.US, "%.0fK views", viewCount / 1_000.0);
        }
        return viewCount + " views";
    }
}