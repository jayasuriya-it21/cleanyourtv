package com.gathin.tvhome.content.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A horizontal shelf of content items displayed on the launcher home screen.
 */
public final class ContentRow {

    public enum RowType {
        POSTER,     // Standard 2:3 vertical poster cards (movies & series)
        BANNER,     // 16:9 landscape cards (continue watching, live channels)
        APP_DOCK,   // Pinned app launcher tiles
        ALL_APPS    // Complete application library
    }

    public final String rowId;
    public final String title;
    public final RowType rowType;
    public final List<ContentItem> items;
    public final boolean isOfflineCached;
    public final boolean requiresNetwork;

    public ContentRow(String rowId, String title, RowType rowType, List<ContentItem> items,
                      boolean isOfflineCached, boolean requiresNetwork) {
        this.rowId = rowId != null ? rowId : "";
        this.title = title != null ? title.trim() : "";
        this.rowType = rowType != null ? rowType : RowType.POSTER;
        this.items = items != null ? Collections.unmodifiableList(new ArrayList<>(items)) : Collections.emptyList();
        this.isOfflineCached = isOfflineCached;
        this.requiresNetwork = requiresNetwork;
    }

    public ContentRow(String rowId, String title, List<ContentItem> items) {
        this(rowId, title, RowType.POSTER, items, false, true);
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public int size() {
        return items != null ? items.size() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentRow that = (ContentRow) o;
        return Objects.equals(rowId, that.rowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowId);
    }

    @Override
    public String toString() {
        return "ContentRow{" +
                "id='" + rowId + '\'' +
                ", title='" + title + '\'' +
                ", size=" + size() +
                '}';
    }
}
