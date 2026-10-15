package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class InAppVideoDialog {

    @SuppressLint("SetJavaScriptEnabled")
    public static void show(Context context, String videoId, String title) {
        if (context == null || videoId == null || videoId.trim().isEmpty()) return;
        try {
            Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.dialog_in_app_video_player);

            TextView tvTitle = dialog.findViewById(R.id.tvVideoTitle);
            if (tvTitle != null && title != null) tvTitle.setText(title);

            View btnClose = dialog.findViewById(R.id.btnClosePlayer);
            WebView webView = dialog.findViewById(R.id.youtubePlayerView);
            ProgressBar progressBar = dialog.findViewById(R.id.videoLoadingProgress);

            if (webView != null) {
                WebSettings settings = webView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setMediaPlaybackRequiresUserGesture(false);
                webView.setWebChromeClient(new WebChromeClient());
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    }
                });

                String embedHtml = "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                        + "<style>body{margin:0;background:#000;display:flex;align-items:center;justify-content:center;height:100vh;}"
                        + "iframe{width:100%;height:100%;border:none;}</style></head><body>"
                        + "<iframe src='https://www.youtube.com/embed/" + videoId.trim() + "?autoplay=1&playsinline=1&rel=0' "
                        + "allow='autoplay; encrypted-media; fullscreen' allowfullscreen></iframe></body></html>";

                webView.loadDataWithBaseURL("https://www.youtube.com", embedHtml, "text/html", "utf-8", null);

                if (btnClose != null) {
                    btnClose.setOnClickListener(v -> {
                        webView.loadUrl("about:blank");
                        webView.destroy();
                        dialog.dismiss();
                    });
                }

                dialog.setOnDismissListener(d -> {
                    try {
                        webView.loadUrl("about:blank");
                        webView.destroy();
                    } catch (Exception ignored) {}
                });
            }

            dialog.show();
        } catch (Exception e) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
            context.startActivity(webIntent);
        }
    }
}