package com.gathin.tvhome.apps;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

import java.util.Objects;

/**
 * Immutable model of an installed application discovered on the TV.
 */
public final class InstalledApp {

    public final String packageName;
    public final String activityName;
    public final ComponentName component;
    public final String label;
    public final Drawable banner;
    public final Drawable icon;
    public final AppCategory category;
    public final String caption;
    public final int accentColor;
    public final boolean isCard;

    public InstalledApp(String packageName, String activityName, ComponentName component,
                        String label, Drawable banner, Drawable icon, AppCategory category,
                        String caption, int accentColor, boolean isCard) {
        this.packageName = packageName != null ? packageName : "";
        this.activityName = activityName != null ? activityName : "";
        this.component = component != null ? component : new ComponentName(this.packageName, this.activityName);
        this.label = label != null ? label : "";
        this.banner = banner;
        this.icon = icon;
        this.category = category != null ? category : AppCategory.TV_NATIVE;
        this.caption = caption != null ? caption : "";
        this.accentColor = accentColor;
        this.isCard = isCard;
    }

    public InstalledApp(String label, ComponentName component, Drawable banner, Drawable icon, AppCategory category) {
        this(component != null ? component.getPackageName() : "",
             component != null ? component.getClassName() : "",
             component, label, banner, icon, category, "", 0, false);
    }

    public boolean isTvNative() {
        return category.isTvNative();
    }

    public boolean requiresTouchWarning() {
        return category.requiresTouchWarning();
    }

    public String getComponentKey() {
        return AppDiscoveryManager.toComponentKey(packageName, activityName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstalledApp that = (InstalledApp) o;
        return Objects.equals(packageName, that.packageName) &&
               Objects.equals(activityName, that.activityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, activityName);
    }

    @Override
    public String toString() {
        return "InstalledApp{" +
                "pkg='" + packageName + '\'' +
                ", act='" + activityName + '\'' +
                ", label='" + label + '\'' +
                ", category=" + category +
                '}';
    }
}
