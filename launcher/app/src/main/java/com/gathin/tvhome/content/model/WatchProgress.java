package com.gathin.tvhome.content.model;

import java.util.Objects;

/**
 * Tracks playback progress for locally-played or app-tracked content.
 */
public final class WatchProgress {

    public final String contentId;
    public final long positionMs;
    public final long durationMs;
    public final long lastEngagedMs;
    public final boolean isCompleted;

    public WatchProgress(String contentId, long positionMs, long durationMs, long lastEngagedMs, boolean isCompleted) {
        this.contentId = contentId != null ? contentId : "";
        this.positionMs = Math.max(0, positionMs);
        this.durationMs = Math.max(0, durationMs);
        this.lastEngagedMs = lastEngagedMs;
        this.isCompleted = isCompleted;
    }

    public int getPercentComplete() {
        if (durationMs <= 0) return 0;
        int pct = (int) ((positionMs * 100) / durationMs);
        return Math.min(100, Math.max(0, pct));
    }

    public boolean isResumeable() {
        if (isCompleted) return false;
        int pct = getPercentComplete();
        return pct >= 3 && pct <= 95;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WatchProgress that = (WatchProgress) o;
        return positionMs == that.positionMs &&
                durationMs == that.durationMs &&
                lastEngagedMs == that.lastEngagedMs &&
                isCompleted == that.isCompleted &&
                Objects.equals(contentId, that.contentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentId, positionMs, durationMs, lastEngagedMs, isCompleted);
    }
}
