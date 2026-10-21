package com.gathin.tvhome.content.model;

import java.util.Objects;

/**
 * Persisted entry in the launcher's local-first watchlist.
 */
public final class UserWatchlistEntry {

    public final String canonicalId;
    public final String title;
    public final ContentType type;
    public final String posterUrl;
    public final String preferredProviderId;
    public final long addedTimestamp;
    public final long lastOpenedTimestamp;

    public UserWatchlistEntry(String canonicalId, String title, ContentType type,
                              String posterUrl, String preferredProviderId,
                              long addedTimestamp, long lastOpenedTimestamp) {
        this.canonicalId = canonicalId != null ? canonicalId : "";
        this.title = title != null ? title.trim() : "";
        this.type = type != null ? type : ContentType.MOVIE;
        this.posterUrl = posterUrl != null ? posterUrl.trim() : "";
        this.preferredProviderId = preferredProviderId != null ? preferredProviderId.trim() : "";
        this.addedTimestamp = addedTimestamp;
        this.lastOpenedTimestamp = lastOpenedTimestamp;
    }

    public static UserWatchlistEntry fromContentItem(ContentItem item, String preferredProviderId) {
        String poster = item.artwork != null ? item.artwork.getPrimaryImageUrl() : "";
        long now = System.currentTimeMillis();
        return new UserWatchlistEntry(item.id, item.title, item.type, poster, preferredProviderId, now, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserWatchlistEntry that = (UserWatchlistEntry) o;
        return Objects.equals(canonicalId, that.canonicalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canonicalId);
    }
}
