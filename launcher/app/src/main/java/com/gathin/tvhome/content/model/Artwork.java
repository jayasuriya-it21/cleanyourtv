package com.gathin.tvhome.content.model;

import java.util.Objects;

/**
 * Image references and sizing hints for content items.
 */
public final class Artwork {

    public final String posterUrl;
    public final String backdropUrl;
    public final String bannerUrl;
    public final int accentColor;

    public Artwork(String posterUrl, String backdropUrl, String bannerUrl, int accentColor) {
        this.posterUrl = posterUrl != null ? posterUrl.trim() : null;
        this.backdropUrl = backdropUrl != null ? backdropUrl.trim() : null;
        this.bannerUrl = bannerUrl != null ? bannerUrl.trim() : null;
        this.accentColor = accentColor;
    }

    public Artwork(String posterUrl, String backdropUrl) {
        this(posterUrl, backdropUrl, null, 0);
    }

    public boolean hasPoster() {
        return posterUrl != null && !posterUrl.isEmpty();
    }

    public boolean hasBackdrop() {
        return backdropUrl != null && !backdropUrl.isEmpty();
    }

    public boolean hasBanner() {
        return bannerUrl != null && !bannerUrl.isEmpty();
    }

    public String getPrimaryImageUrl() {
        if (hasPoster()) return posterUrl;
        if (hasBanner()) return bannerUrl;
        return backdropUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artwork artwork = (Artwork) o;
        return accentColor == artwork.accentColor &&
                Objects.equals(posterUrl, artwork.posterUrl) &&
                Objects.equals(backdropUrl, artwork.backdropUrl) &&
                Objects.equals(bannerUrl, artwork.bannerUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posterUrl, backdropUrl, bannerUrl, accentColor);
    }

    @Override
    public String toString() {
        return "Artwork{" +
                "posterUrl='" + posterUrl + '\'' +
                ", backdropUrl='" + backdropUrl + '\'' +
                ", bannerUrl='" + bannerUrl + '\'' +
                ", accentColor=" + accentColor +
                '}';
    }
}
