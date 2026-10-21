package com.gathin.tvhome.content.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Universal content model representing a movie, series, episode, or synthetic title.
 * Immutable and self-contained for local-first operations.
 */
public final class ContentItem {

    public final String id;
    public final String title;
    public final ContentType type;
    public final int releaseYear;
    public final String contentRating;
    public final int durationMinutes;
    public final String synopsis;
    public final String primaryLanguage;
    public final List<String> genres;
    public final Artwork artwork;
    public final String attribution;
    public final ContentIdentity identity;
    public final WatchProgress progress;

    public ContentItem(String id, String title, ContentType type, int releaseYear,
                       String contentRating, int durationMinutes, String synopsis,
                       String primaryLanguage, List<String> genres, Artwork artwork,
                       String attribution, ContentIdentity identity, WatchProgress progress) {
        this.id = id != null ? id : "";
        this.title = title != null ? title.trim() : "";
        this.type = type != null ? type : ContentType.MOVIE;
        this.releaseYear = releaseYear;
        this.contentRating = contentRating != null ? contentRating.trim() : "";
        this.durationMinutes = Math.max(0, durationMinutes);
        this.synopsis = synopsis != null ? synopsis.trim() : "";
        this.primaryLanguage = primaryLanguage != null ? primaryLanguage.trim() : "en";
        this.genres = genres != null ? Collections.unmodifiableList(new ArrayList<>(genres)) : Collections.emptyList();
        this.artwork = artwork != null ? artwork : new Artwork(null, null);
        this.attribution = attribution != null ? attribution.trim() : "";
        this.identity = identity != null ? identity : ContentIdentity.local(this.id);
        this.progress = progress;
    }

    public static ContentItem createSynthetic(String id, String title, ContentType type, int releaseYear,
                                              String contentRating, String synopsis, String language) {
        return new ContentItem(id, title, type, releaseYear, contentRating, 0, synopsis, language,
                Collections.emptyList(), new Artwork(null, null), "", ContentIdentity.synthetic(id), null);
    }

    public boolean hasArtwork() {
        return artwork != null && (artwork.hasPoster() || artwork.hasBackdrop() || artwork.hasBanner());
    }

    public String getMetaLine() {
        StringBuilder sb = new StringBuilder();
        if (releaseYear > 0) sb.append(releaseYear);
        if (!contentRating.isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(contentRating);
        }
        if (durationMinutes > 0) {
            if (sb.length() > 0) sb.append(" · ");
            int h = durationMinutes / 60;
            int m = durationMinutes % 60;
            if (h > 0) sb.append(h).append("h ");
            if (m > 0 || h == 0) sb.append(m).append("m");
        }
        if (primaryLanguage != null && !primaryLanguage.isEmpty()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(primaryLanguage.toUpperCase(java.util.Locale.ROOT));
        }
        return sb.toString();
    }

    public ContentItem withProgress(WatchProgress newProgress) {
        return new ContentItem(id, title, type, releaseYear, contentRating, durationMinutes,
                synopsis, primaryLanguage, genres, artwork, attribution, identity, newProgress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentItem that = (ContentItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ContentItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", year=" + releaseYear +
                '}';
    }
}
