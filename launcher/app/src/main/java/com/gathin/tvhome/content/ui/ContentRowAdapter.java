package com.gathin.tvhome.content.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gathin.tvhome.R;
import com.gathin.tvhome.content.model.ContentItem;

import java.util.Collections;
import java.util.List;

/**
 * Lightweight horizontal RecyclerView adapter for synthetic content shelves.
 * Uses bundled XML vector/shape drawables, stable IDs, and zero network calls.
 */
public class ContentRowAdapter extends RecyclerView.Adapter<ContentRowAdapter.ViewHolder> {

    public interface OnPosterListener {
        void onPosterFocused(int position, ContentItem item, View view);
        void onPosterClicked(int position, ContentItem item, View view);
        boolean onPosterKey(int position, int keyCode, KeyEvent event);
    }

    private final List<ContentItem> items;
    private final OnPosterListener listener;

    public ContentRowAdapter(List<ContentItem> items, OnPosterListener listener) {
        this.items = items != null ? items : Collections.emptyList();
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= items.size()) {
            return RecyclerView.NO_ID;
        }
        ContentItem item = items.get(position);
        return item != null ? stableId(item.id) : position;
    }

    /**
     * Pure Java stable long ID generation for content items.
     */
    public static long stableId(String id) {
        if (id == null || id.isEmpty()) return 0L;
        long h = 1125899906842597L; // FNV-like prime seed
        for (int i = 0; i < id.length(); i++) {
            h = 31 * h + id.charAt(i);
        }
        return h & 0x7FFFFFFFFFFFFFFFL;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_content_poster, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContentItem item = items.get(position);
        if (item == null) return;

        Context context = holder.itemView.getContext();

        holder.titleView.setText(item.title);
        holder.metaView.setText(item.getMetaLine());

        // Accessibility content description
        String contentDesc = context.getString(
                R.string.spotlight_content_desc,
                item.title,
                item.releaseYear,
                item.synopsis
        );
        holder.itemView.setContentDescription(contentDesc);

        // Resolve bundled local XML drawable
        if (item.artwork != null && item.artwork.hasPoster()) {
            int resId = resolvePosterDrawable(item.artwork.posterUrl);
            if (resId != 0) {
                Drawable drawable = ContextCompat.getDrawable(context, resId);
                holder.artView.setImageDrawable(drawable);
            } else {
                holder.artView.setImageDrawable(null);
            }
        } else {
            holder.artView.setImageDrawable(null);
        }

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            float scale = hasFocus ? 1.06f : 1.0f;
            v.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(150)
                    .start();

            if (hasFocus && listener != null) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onPosterFocused(adapterPos, item, v);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onPosterClicked(adapterPos, item, v);
                }
            }
        });

        holder.itemView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && listener != null) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    return listener.onPosterKey(adapterPos, keyCode, event);
                }
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public ContentItem getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    public static int resolvePosterDrawable(String posterUrl) {
        if ("poster_chennai_rain".equals(posterUrl)) return R.drawable.poster_chennai_rain;
        if ("poster_bus_home".equals(posterUrl)) return R.drawable.poster_bus_home;
        if ("poster_marina_lights".equals(posterUrl)) return R.drawable.poster_marina_lights;
        if ("poster_silent_temple".equals(posterUrl)) return R.drawable.poster_silent_temple;
        if ("poster_river_stars".equals(posterUrl)) return R.drawable.poster_river_stars;
        if ("poster_southern_signal".equals(posterUrl)) return R.drawable.poster_southern_signal;
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView artView;
        public final TextView titleView;
        public final TextView metaView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artView = itemView.findViewById(R.id.posterArt);
            titleView = itemView.findViewById(R.id.posterTitle);
            metaView = itemView.findViewById(R.id.posterMeta);
        }
    }
}
