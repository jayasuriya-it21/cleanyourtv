package com.gathin.tvhome.content.model;

import java.util.Collections;
import java.util.List;

/**
 * Result wrapper for metadata queries with cache status and error reporting.
 */
public final class ContentSourceResult {

    public final boolean isSuccess;
    public final List<ContentRow> rows;
    public final List<ContentItem> items;
    public final boolean isFromCache;
    public final String errorMessage;

    public static ContentSourceResult successRows(List<ContentRow> rows, boolean isFromCache) {
        return new ContentSourceResult(true, rows, null, isFromCache, null);
    }

    public static ContentSourceResult successItems(List<ContentItem> items, boolean isFromCache) {
        return new ContentSourceResult(true, null, items, isFromCache, null);
    }

    public static ContentSourceResult failure(String errorMessage) {
        return new ContentSourceResult(false, null, null, false, errorMessage);
    }

    private ContentSourceResult(boolean isSuccess, List<ContentRow> rows, List<ContentItem> items,
                                boolean isFromCache, String errorMessage) {
        this.isSuccess = isSuccess;
        this.rows = rows != null ? Collections.unmodifiableList(new java.util.ArrayList<>(rows)) : Collections.emptyList();
        this.items = items != null ? Collections.unmodifiableList(new java.util.ArrayList<>(items)) : Collections.emptyList();
        this.isFromCache = isFromCache;
        this.errorMessage = errorMessage != null ? errorMessage : "";
    }
}
