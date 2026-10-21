package com.gathin.tvhome.content.demo;

import android.content.Context;

import com.gathin.tvhome.R;
import com.gathin.tvhome.content.model.Artwork;
import com.gathin.tvhome.content.model.ContentIdentity;
import com.gathin.tvhome.content.model.ContentItem;
import com.gathin.tvhome.content.model.ContentRow;
import com.gathin.tvhome.content.model.ContentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates synthetic, offline-only demonstration content for the Tamil Spotlight shelf.
 * All titles, synopses, and art assets are local original placeholders and contain
 * zero provider identity, zero deep links, and zero network dependencies.
 */
public final class SyntheticContentFactory {

    public static final String ROW_ID = "synthetic_spotlight_tamil";
    public static final String ROW_TITLE = "Tamil Spotlight";
    public static final int ITEM_COUNT = 6;

    public static final String ID_CHENNAI_RAIN = "synthetic_spotlight_1";
    public static final String ID_LAST_BUS = "synthetic_spotlight_2";
    public static final String ID_MARINA_LIGHTS = "synthetic_spotlight_3";
    public static final String ID_SILENT_TEMPLE = "synthetic_spotlight_4";
    public static final String ID_RIVER_STARS = "synthetic_spotlight_5";
    public static final String ID_SOUTHERN_SIGNAL = "synthetic_spotlight_6";

    private SyntheticContentFactory() {
    }

    /**
     * Creates the complete synthetic Tamil Spotlight ContentRow.
     */
    public static ContentRow createSpotlightRow(Context context) {
        return new ContentRow(
                ROW_ID,
                context != null ? context.getString(R.string.spotlight_title) : ROW_TITLE,
                ContentRow.RowType.POSTER,
                createSpotlightItems(context),
                true,   // isOfflineCached
                false   // requiresNetwork
        );
    }

    /**
     * Creates the list of 6 synthetic demonstration items.
     * Safe to invoke with null context in unit test environments.
     */
    public static List<ContentItem> createSpotlightItems(Context context) {
        List<ContentItem> items = new ArrayList<>(ITEM_COUNT);

        items.add(createItem(
                ID_CHENNAI_RAIN,
                context != null ? context.getString(R.string.spotlight_item_1_title) : "Chennai After Rain",
                context != null ? context.getString(R.string.spotlight_item_1_synopsis)
                        : "A reflective journey through glistening monsoon streets and quiet city moments.",
                2024,
                "poster_chennai_rain"
        ));

        items.add(createItem(
                ID_LAST_BUS,
                context != null ? context.getString(R.string.spotlight_item_2_title) : "The Last Bus Home",
                context != null ? context.getString(R.string.spotlight_item_2_synopsis)
                        : "Late-night travelers find unexpected stories and shared warmth on a midnight commute.",
                2023,
                "poster_bus_home"
        ));

        items.add(createItem(
                ID_MARINA_LIGHTS,
                context != null ? context.getString(R.string.spotlight_item_3_title) : "Marina Lights",
                context != null ? context.getString(R.string.spotlight_item_3_synopsis)
                        : "Vibrant coastal evenings captured through the lens of local seaside wanderers.",
                2024,
                "poster_marina_lights"
        ));

        items.add(createItem(
                ID_SILENT_TEMPLE,
                context != null ? context.getString(R.string.spotlight_item_4_title) : "Silent Temple",
                context != null ? context.getString(R.string.spotlight_item_4_synopsis)
                        : "Centuries of heritage and tranquil dawn rituals carved into ancient granite courtyards.",
                2022,
                "poster_silent_temple"
        ));

        items.add(createItem(
                ID_RIVER_STARS,
                context != null ? context.getString(R.string.spotlight_item_5_title) : "River of Stars",
                context != null ? context.getString(R.string.spotlight_item_5_synopsis)
                        : "Astronomy enthusiasts document the pristine night sky above southern hill country.",
                2023,
                "poster_river_stars"
        ));

        items.add(createItem(
                ID_SOUTHERN_SIGNAL,
                context != null ? context.getString(R.string.spotlight_item_6_title) : "Southern Signal",
                context != null ? context.getString(R.string.spotlight_item_6_synopsis)
                        : "A lone railway operator decodes a series of nostalgic transmissions along forgotten tracks.",
                2024,
                "poster_southern_signal"
        ));

        return Collections.unmodifiableList(items);
    }

    private static ContentItem createItem(String id, String title, String synopsis, int year, String drawableName) {
        Artwork artwork = new Artwork(drawableName, null, null, 0);
        ContentIdentity identity = ContentIdentity.synthetic(id);
        return new ContentItem(
                id,
                title,
                ContentType.MOVIE,
                year,
                "U",
                0,
                synopsis,
                "ta",
                Collections.emptyList(),
                artwork,
                "CleanYourTV Prototype",
                identity,
                null
        );
    }
}
