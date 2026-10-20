package com.example.myapplication.data.model;

import java.util.Locale;

public class YouTubeEyeVideo {
    private final String videoId;
    private final String title;
    private final String channelTitle;
    private final String channelThumbnail;
    private final String videoThumbnail;
    private final long viewCount;
    private final String formattedDuration;
    private final String publishedTimeText;
    private final String category;

    public YouTubeEyeVideo(String videoId, String title, String channelTitle,
                           String channelThumbnail, String videoThumbnail,
                           long viewCount, String formattedDuration,
                           String publishedTimeText, String category) {
        this.videoId = videoId;
        this.title = title;
        this.channelTitle = channelTitle;
        this.channelThumbnail = channelThumbnail;
        this.videoThumbnail = videoThumbnail;
        this.viewCount = viewCount;
        this.formattedDuration = formattedDuration;
        this.publishedTimeText = publishedTimeText;
        this.category = category;
    }

    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
    public String getChannelTitle() { return channelTitle; }
    public String getChannelThumbnail() { return channelThumbnail; }
    public String getVideoThumbnail() { return videoThumbnail; }
    public long getViewCount() { return viewCount; }
    public String getFormattedDuration() { return formattedDuration; }
    public String getPublishedTimeText() { return publishedTimeText; }
    public String getCategory() { return category; }

    public String getFormattedViews() {
        if (viewCount >= 1_000_000) {
            return String.format(Locale.US, "%.1fM views", viewCount / 1_000_000.0).replace(".0M", "M");
        } else if (viewCount >= 1_000) {
            return String.format(Locale.US, "%.0fK views", viewCount / 1_000.0);
        }
        return viewCount + " views";
    }
}