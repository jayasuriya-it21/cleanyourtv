package com.gathin.tvhome.content.model;

import java.util.Objects;

/**
 * Disambiguates external metadata IDs from third-party provider content identifiers.
 * Prevents metadata IDs (e.g. TMDB) from ever being conflated with provider-specific IDs.
 */
public final class ContentIdentity {

    public final String canonicalId;
    public final String metadataSource;
    public final String providerId;
    public final String providerContentId;
    public final String providerWebUrl;
    public final String candidateDeepLinkUri;
    public final String region;
    public final long availabilityTimestamp;

    public ContentIdentity(String canonicalId, String metadataSource, String providerId,
                           String providerContentId, String providerWebUrl,
                           String candidateDeepLinkUri, String region,
                           long availabilityTimestamp) {
        this.canonicalId = canonicalId != null ? canonicalId : "";
        this.metadataSource = metadataSource != null ? metadataSource : "";
        this.providerId = providerId != null ? providerId : "";
        this.providerContentId = providerContentId != null ? providerContentId : "";
        this.providerWebUrl = providerWebUrl != null ? providerWebUrl : "";
        this.candidateDeepLinkUri = candidateDeepLinkUri != null ? candidateDeepLinkUri : "";
        this.region = region != null ? region : "IN";
        this.availabilityTimestamp = availabilityTimestamp;
    }

    public static ContentIdentity local(String id) {
        return new ContentIdentity(id, "local", "", "", "", "", "IN", System.currentTimeMillis());
    }

    public static ContentIdentity synthetic(String id) {
        return new ContentIdentity(id, "synthetic", "", "", "", "", "IN", System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContentIdentity that = (ContentIdentity) o;
        return availabilityTimestamp == that.availabilityTimestamp &&
                Objects.equals(canonicalId, that.canonicalId) &&
                Objects.equals(metadataSource, that.metadataSource) &&
                Objects.equals(providerId, that.providerId) &&
                Objects.equals(providerContentId, that.providerContentId) &&
                Objects.equals(region, that.region);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canonicalId, metadataSource, providerId, providerContentId, region, availabilityTimestamp);
    }

    @Override
    public String toString() {
        return "ContentIdentity{" +
                "canonicalId='" + canonicalId + '\'' +
                ", source='" + metadataSource + '\'' +
                ", providerId='" + providerId + '\'' +
                ", providerContentId='" + providerContentId + '\'' +
                '}';
    }
}
