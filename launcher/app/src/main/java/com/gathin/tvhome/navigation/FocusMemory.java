package com.gathin.tvhome.navigation;

import com.gathin.tvhome.content.model.ContentItem;

import java.util.List;

/**
 * Tracks and restores remote focus position and identity across navigation boundaries,
 * activity switches, and dialog dismissals.
 * Provides deterministic fallbacks when items move or are removed.
 */
public final class FocusMemory {

    private int lastFocusedIndex = 0;
    private String lastFocusedId = "";
    private boolean hasFocus = false;

    public FocusMemory() {
    }

    public FocusMemory(int initialIndex, String initialId) {
        this.lastFocusedIndex = Math.max(0, initialIndex);
        this.lastFocusedId = initialId != null ? initialId : "";
        this.hasFocus = true;
    }

    /**
     * Records the currently focused position and item ID.
     */
    public synchronized void recordFocus(int position, String id) {
        this.lastFocusedIndex = Math.max(0, position);
        this.lastFocusedId = id != null ? id : "";
        this.hasFocus = true;
    }

    /**
     * Determines the optimal index to restore focus to within the provided items.
     * Hierarchy:
     * 1. Match by stable item ID.
     * 2. If ID not found, match by previous index clamped to valid range.
     * 3. Default fallback to 0.
     *
     * @param items Current list of content items.
     * @return 0-based target index, or -1 if the list is null or empty.
     */
    public synchronized int resolveRestorationIndex(List<ContentItem> items) {
        if (items == null || items.isEmpty()) {
            return -1;
        }

        // 1. Try matching by stable ID
        if (!lastFocusedId.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                ContentItem item = items.get(i);
                if (item != null && lastFocusedId.equals(item.id)) {
                    lastFocusedIndex = i;
                    return i;
                }
            }
        }

        // 2. Clamped position fallback
        int clamped = Math.max(0, Math.min(lastFocusedIndex, items.size() - 1));
        lastFocusedIndex = clamped;
        return clamped;
    }

    public synchronized int getLastFocusedIndex() {
        return lastFocusedIndex;
    }

    public synchronized String getLastFocusedId() {
        return lastFocusedId;
    }

    public synchronized boolean hasRecordedFocus() {
        return hasFocus;
    }

    /**
     * Determines whether focus should be restored.
     * Returns false if the feature is disabled, if no focus was recorded,
     * if the user has already moved focus to another view before restoration executed,
     * or if the host activity is finishing.
     */
    public synchronized boolean shouldRestoreFocus(boolean featureEnabled, boolean userMovedFocus, boolean isFinishing) {
        if (!featureEnabled || !hasFocus || userMovedFocus || isFinishing) {
            return false;
        }
        return true;
    }

    /**
     * Resolves restoration position with safety gates for feature toggle and user focus movement.
     * If feature is disabled, clears memory and returns -1.
     * If user already moved focus or has no recorded focus, returns -1.
     * If items list is empty, returns -1.
     */
    public synchronized int resolveRestoration(boolean featureEnabled, List<ContentItem> items, boolean userMovedFocus) {
        if (!featureEnabled) {
            clear();
            return -1;
        }
        if (userMovedFocus || !hasFocus) {
            return -1;
        }
        return resolveRestorationIndex(items);
    }

    public synchronized void clear() {
        lastFocusedIndex = 0;
        lastFocusedId = "";
        hasFocus = false;
    }
}
