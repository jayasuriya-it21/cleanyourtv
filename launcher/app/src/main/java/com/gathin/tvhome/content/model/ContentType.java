package com.gathin.tvhome.content.model;

/**
 * Supported content classifications for launcher items.
 */
public enum ContentType {
    MOVIE,
    TV_SHOW,
    EPISODE,
    LIVE_CHANNEL,
    LOCAL_VIDEO,
    APP;

    public boolean isVideo() {
        return this == MOVIE || this == TV_SHOW || this == EPISODE || this == LOCAL_VIDEO;
    }
}
