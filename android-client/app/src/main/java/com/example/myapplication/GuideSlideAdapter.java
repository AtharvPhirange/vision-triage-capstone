package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GuideSlideAdapter extends RecyclerView.Adapter<GuideSlideAdapter.ViewHolder> {

    public static class SlideData {
        String badgeText;
        String badgeColorHex;
        String title;
        String caption;
        String subTitle1, subDesc1;
        String subTitle2, subDesc2;
        String subTitle3, subDesc3;
        String subTitle4, subDesc4;

        public SlideData(String badgeText, String badgeColorHex, String title, String caption,
                         String subTitle1, String subDesc1, String subTitle2, String subDesc2,
                         String subTitle3, String subDesc3, String subTitle4, String subDesc4) {
            this.badgeText = badgeText;
            this.badgeColorHex = badgeColorHex;
            this.title = title;
            this.caption = caption;
            this.subTitle1 = subTitle1;
            this.subDesc1 = subDesc1;
            this.subTitle2 = subTitle2;
            this.subDesc2 = subDesc2;
            this.subTitle3 = subTitle3;
            this.subDesc3 = subDesc3;
            this.subTitle4 = subTitle4;
            this.subDesc4 = subDesc4;
        }
    }

    private final List<SlideData> slides;

    public GuideSlideAdapter(List<SlideData> slides) {
        this.slides = slides;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guide_step_slide, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SlideData data = slides.get(position);
        if (data != null) {
            if (holder.tvSlideBadge != null) {
                holder.tvSlideBadge.setText(data.badgeText);
                try {
                    holder.tvSlideBadge.setTextColor(Color.parseColor(data.badgeColorHex));
                } catch (Exception ignored) {}
            }

            if (holder.tvSlideTitle != null) {
                holder.tvSlideTitle.setText(data.title);
            }

            if (holder.tvSlideCaption != null) {
                holder.tvSlideCaption.setText(data.caption);
            }

            if (holder.tvSubTitle1 != null) holder.tvSubTitle1.setText(data.subTitle1);
            if (holder.tvSubDesc1 != null) holder.tvSubDesc1.setText(data.subDesc1);

            if (holder.tvSubTitle2 != null) holder.tvSubTitle2.setText(data.subTitle2);
            if (holder.tvSubDesc2 != null) holder.tvSubDesc2.setText(data.subDesc2);

            if (holder.tvSubTitle3 != null) holder.tvSubTitle3.setText(data.subTitle3);
            if (holder.tvSubDesc3 != null) holder.tvSubDesc3.setText(data.subDesc3);

            if (holder.tvSubTitle4 != null) holder.tvSubTitle4.setText(data.subTitle4);
            if (holder.tvSubDesc4 != null) holder.tvSubDesc4.setText(data.subDesc4);
        }
    }

    @Override
    public int getItemCount() {
        return slides != null ? slides.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSlideBadge, tvSlideTitle, tvSlideCaption;
        TextView tvSubTitle1, tvSubDesc1;
        TextView tvSubTitle2, tvSubDesc2;
        TextView tvSubTitle3, tvSubDesc3;
        TextView tvSubTitle4, tvSubDesc4;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSlideBadge = itemView.findViewById(R.id.tvSlideBadge);
            tvSlideTitle = itemView.findViewById(R.id.tvSlideTitle);
            tvSlideCaption = itemView.findViewById(R.id.tvSlideCaption);

            tvSubTitle1 = itemView.findViewById(R.id.tvSubTitle1);
            tvSubDesc1 = itemView.findViewById(R.id.tvSubDesc1);

            tvSubTitle2 = itemView.findViewById(R.id.tvSubTitle2);
            tvSubDesc2 = itemView.findViewById(R.id.tvSubDesc2);

            tvSubTitle3 = itemView.findViewById(R.id.tvSubTitle3);
            tvSubDesc3 = itemView.findViewById(R.id.tvSubDesc3);

            tvSubTitle4 = itemView.findViewById(R.id.tvSubTitle4);
            tvSubDesc4 = itemView.findViewById(R.id.tvSubDesc4);
        }
    }
}