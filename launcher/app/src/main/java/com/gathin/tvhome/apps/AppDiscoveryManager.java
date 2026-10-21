package com.gathin.tvhome.apps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.gathin.tvhome.Cards;
import com.gathin.tvhome.LibraryApps;
import com.gathin.tvhome.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Extracts, consolidates, and normalizes application discovery from PackageManager.
 * Provides pure normalization logic separated from Android framework dependencies for testability.
 */
public final class AppDiscoveryManager {

    /**
     * Pure Java descriptor of an activity discovered from Intent queries.
     */
    public static final class RawActivity {
        public final String packageName;
        public final String activityName;
        public final String label;
        public final Drawable banner;
        public final Drawable icon;
        public final boolean isLeanback;
        public final boolean isExported;
        public final boolean isEnabled;
        public final boolean isResolvable;

        public RawActivity(String packageName, String activityName, String label,
                           Drawable banner, Drawable icon, boolean isLeanback,
                           boolean isExported, boolean isEnabled, boolean isResolvable) {
            this.packageName = packageName != null ? packageName : "";
            this.activityName = activityName != null ? activityName : "";
            this.label = label != null ? label : "";
            this.banner = banner;
            this.icon = icon;
            this.isLeanback = isLeanback;
            this.isExported = isExported;
            this.isEnabled = isEnabled;
            this.isResolvable = isResolvable;
        }

        public static RawActivity createSimple(String packageName, String activityName, String label,
                                               boolean isLeanback, boolean isExported, boolean isEnabled) {
            return new RawActivity(packageName, activityName, label, null, null, isLeanback, isExported, isEnabled, true);
        }

        public ComponentName getComponent() {
            return new ComponentName(packageName, activityName);
        }
    }

    /**
     * Configuration parameters controlling application discovery, filtering, and ordering.
     */
    public static final class DiscoveryConfig {
        public final String selfPackageName;
        public final Set<String> hiddenPackages;
        public final List<String> dockPackages;
        public final Map<String, Integer> savedRanks;
        public final Set<String> libraryAdded;
        public final Set<String> libraryRemoved;
        public final Map<String, String> appLabels;
        public final Map<String, String> appBanners;
        public final Map<String, Cards.Card> cards;

        public DiscoveryConfig(String selfPackageName, Set<String> hiddenPackages,
                               List<String> dockPackages, Map<String, Integer> savedRanks,
                               Set<String> libraryAdded, Set<String> libraryRemoved,
                               Map<String, String> appLabels, Map<String, String> appBanners,
                               Map<String, Cards.Card> cards) {
            this.selfPackageName = selfPackageName != null ? selfPackageName : "";
            this.hiddenPackages = hiddenPackages != null ? new HashSet<>(hiddenPackages) : Collections.emptySet();
            this.dockPackages = dockPackages != null ? new ArrayList<>(dockPackages) : Collections.emptyList();
            this.savedRanks = savedRanks != null ? new HashMap<>(savedRanks) : Collections.emptyMap();
            this.libraryAdded = libraryAdded != null ? new HashSet<>(libraryAdded) : Collections.emptySet();
            this.libraryRemoved = libraryRemoved != null ? new HashSet<>(libraryRemoved) : Collections.emptySet();
            this.appLabels = appLabels != null ? new HashMap<>(appLabels) : Collections.emptyMap();
            this.appBanners = appBanners != null ? new HashMap<>(appBanners) : Collections.emptyMap();
            this.cards = cards != null ? new HashMap<>(cards) : Collections.emptyMap();
        }
    }

    /**
     * Pure normalization algorithm.
     * Takes raw activity descriptors and produces deduplicated, categorized, and sorted InstalledApps.
     */
    public static List<InstalledApp> normalize(List<RawActivity> rawActivities, DiscoveryConfig config) {
        if (rawActivities == null || rawActivities.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Identify which packages declare at least one valid Leanback activity.
        Set<String> tvPackages = new HashSet<>();
        for (RawActivity raw : rawActivities) {
            if (raw.isLeanback && raw.isExported && raw.isEnabled && raw.isResolvable
                    && !raw.packageName.equals(config.selfPackageName)) {
                tvPackages.add(raw.packageName);
            }
        }

        // 2. Filter, deduplicate, prefer Leanback, and classify.
        Map<String, InstalledApp> uniqueApps = new HashMap<>();
        Set<String> seenPkgAndLabel = new HashSet<>();
        Set<String> seenComponents = new HashSet<>();

        for (RawActivity raw : rawActivities) {
            // Safety: Skip invalid, disabled, unexported, unresolvable, or self package
            if (raw.packageName.isEmpty() || raw.activityName.isEmpty()
                    || !raw.isEnabled || !raw.isExported || !raw.isResolvable
                    || raw.packageName.equals(config.selfPackageName)) {
                continue;
            }

            String pkg = raw.packageName;
            String compKey = toComponentKey(pkg, raw.activityName);

            // Sideloaded preference: If package exposes Leanback, drop phone-only launcher activities
            if (!raw.isLeanback && tvPackages.contains(pkg)) {
                continue;
            }

            // Deduplicate exact duplicate component records
            if (!seenComponents.add(compKey)) {
                continue;
            }

            // Respect AppLibrary visibility (Leanback TV apps on Home unless removed; sideloaded only if added; hidden wins)
            boolean isTv = tvPackages.contains(pkg);
            boolean isHidden = config.hiddenPackages.contains(pkg);
            boolean isAdded = config.libraryAdded.contains(pkg);
            boolean isRemoved = config.libraryRemoved.contains(pkg);
            if (!LibraryApps.visible(isTv, isHidden, isAdded, isRemoved)) {
                continue;
            }

            // Check if third-party Module Card stands in for this tile
            Cards.Card card = config.cards.get(pkg);
            if (card != null && !isCardComponent(card, pkg, raw.activityName)) {
                continue;
            }

            // Label cleaning and override
            String label;
            if (card != null) {
                label = card.label;
            } else if (config.appLabels.containsKey(pkg)) {
                label = Cards.cleanLabel(config.appLabels.get(pkg), pkg);
            } else {
                label = Cards.cleanLabel(raw.label, pkg);
            }

            // Some system apps declare two leanback entry points under one label (e.g. Sony Timers & Clock)
            if (!seenPkgAndLabel.add(pkg + '\n' + label)) {
                continue;
            }

            // Categorization
            AppCategory category = isTv ? AppCategory.TV_NATIVE : AppCategory.SIDELOADED_TOUCH;

            ComponentName component = raw.getComponent();
            InstalledApp app;
            if (card != null) {
                app = new InstalledApp(pkg, raw.activityName, component, label,
                        raw.banner, raw.icon, category, card.caption, card.accent, true);
            } else {
                app = new InstalledApp(pkg, raw.activityName, component, label,
                        raw.banner, raw.icon, category, "", 0, false);
            }

            uniqueApps.put(compKey, app);
        }

        List<InstalledApp> result = new ArrayList<>(uniqueApps.values());

        // 3. Sort deterministically
        Collections.sort(result, (a, b) -> {
            // First: Saved user custom order (Order.java)
            int saved = compareSavedOrder(config.savedRanks,
                    a.packageName, a.activityName,
                    b.packageName, b.activityName);
            if (saved != 0) return saved;

            // Second: Pinned dock favorites order
            int fa = config.dockPackages.indexOf(a.packageName);
            int fb = config.dockPackages.indexOf(b.packageName);
            if (fa >= 0 || fb >= 0) {
                if (fa < 0) return 1;
                if (fb < 0) return -1;
                return Integer.compare(fa, fb);
            }

            // Third: Module cards come right after dock apps before alphabetical grid
            if (a.isCard != b.isCard) {
                return a.isCard ? -1 : 1;
            }

            // Fourth: Alphabetical by label
            int byLabel = a.label.compareToIgnoreCase(b.label);
            if (byLabel != 0) return byLabel;

            // Tie breaker: Package name
            return a.packageName.compareTo(b.packageName);
        });

        return Collections.unmodifiableList(result);
    }

    /**
     * Flattens a package and class name into standard component key representation.
     * Pure Java implementation matching ComponentName.flattenToShortString().
     */
    public static String toComponentKey(String packageName, String activityName) {
        if (packageName == null) packageName = "";
        if (activityName == null) activityName = "";
        if (activityName.startsWith(".")) {
            return packageName + "/" + activityName;
        }
        if (activityName.startsWith(packageName) && activityName.length() > packageName.length()
                && activityName.charAt(packageName.length()) == '.') {
            return packageName + "/" + activityName.substring(packageName.length());
        }
        return packageName + "/" + activityName;
    }

    private static boolean isCardComponent(Cards.Card card, String pkg, String act) {
        if (card == null || card.component == null) return true;
        try {
            String cardPkg = card.component.getPackageName();
            String cardCls = card.component.getClassName();
            if (cardPkg != null && !cardPkg.equals(pkg)) return false;
            if (cardCls != null && !cardCls.equals(act)) return false;
            return true;
        } catch (RuntimeException ignored) {
            return card.pkg == null || card.pkg.equals(pkg);
        }
    }

    private static int compareSavedOrder(Map<String, Integer> ranks, String pkgA, String actA, String pkgB, String actB) {
        if (ranks == null || ranks.isEmpty()) return 0;
        Integer a = getRank(ranks, pkgA, actA);
        Integer b = getRank(ranks, pkgB, actB);
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return Integer.compare(a, b);
    }

    private static Integer getRank(Map<String, Integer> ranks, String pkg, String act) {
        String shortKey = toComponentKey(pkg, act);
        Integer r = ranks.get(shortKey);
        if (r != null) return r;
        return ranks.get(pkg + "/" + act);
    }

    /**
     * Discovers all launchable Home apps from Android's PackageManager.
     */
    public static List<InstalledApp> discoverHomeApps(Context context,
                                                      DiscoveryConfig config,
                                                      DrawableResolver drawableResolver) {
        PackageManager pm = context.getPackageManager();
        List<RawActivity> rawList = new ArrayList<>();

        // 1. Query Leanback launcher apps
        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> leanbackResolves = pm.queryIntentActivities(leanbackIntent, 0);
        Set<String> tvPackages = new HashSet<>();
        for (ResolveInfo ri : leanbackResolves) {
            if (ri.activityInfo != null) {
                tvPackages.add(ri.activityInfo.packageName);
                rawList.add(toRawActivity(pm, ri, true, drawableResolver, config.appBanners));
            }
        }

        // 2. Query Launcher apps for sideloaded additions (only if user added them)
        if (!config.libraryAdded.isEmpty()) {
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> launcherResolves = pm.queryIntentActivities(launcherIntent, 0);
            for (ResolveInfo ri : launcherResolves) {
                if (ri.activityInfo != null && config.libraryAdded.contains(ri.activityInfo.packageName)
                        && !tvPackages.contains(ri.activityInfo.packageName)) {
                    rawList.add(toRawActivity(pm, ri, false, drawableResolver, config.appBanners));
                }
            }
        }

        return normalize(rawList, config);
    }

    private static RawActivity toRawActivity(PackageManager pm, ResolveInfo ri, boolean isLeanback,
                                             DrawableResolver resolver, Map<String, String> appBanners) {
        String pkg = ri.activityInfo.packageName;
        String name = ri.activityInfo.name;
        String label = String.valueOf(ri.loadLabel(pm));
        boolean exported = ri.activityInfo.exported;
        boolean enabled = ri.activityInfo.isEnabled();

        Drawable banner = null;
        if (resolver != null && appBanners != null && appBanners.containsKey(pkg)) {
            banner = resolver.resolveBundledBanner(appBanners.get(pkg));
        }
        if (banner == null) {
            banner = ri.activityInfo.loadBanner(pm);
        }
        Drawable icon = ri.loadIcon(pm);

        return new RawActivity(pkg, name, label, banner, icon, isLeanback, exported, enabled, true);
    }

    /**
     * Pure Java normalization for App Library discovery.
     * Preserves broader behavior: includes Leanback and Launcher activities, prefers Leanback for dual packages,
     * drops self package and unexported/disabled activities, and orders by LibraryApps priority and label.
     */
    public static List<RawActivity> normalizeLibrary(List<RawActivity> rawActivities,
                                                     String selfPackageName,
                                                     java.util.function.Predicate<String> onHome,
                                                     Map<String, Long> installTimes,
                                                     long catalogTime,
                                                     Set<String> outTvPackages) {
        if (rawActivities == null || rawActivities.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, RawActivity> unique = new java.util.LinkedHashMap<>();
        if (outTvPackages != null) outTvPackages.clear();

        // 1. First pass: Leanback activities
        for (RawActivity raw : rawActivities) {
            if (!raw.isLeanback || raw.packageName.isEmpty() || raw.activityName.isEmpty()
                    || !raw.isExported || !raw.isEnabled || !raw.isResolvable
                    || raw.packageName.equals(selfPackageName)) {
                continue;
            }
            if (outTvPackages != null) outTvPackages.add(raw.packageName);
            if (!unique.containsKey(raw.packageName)) {
                unique.put(raw.packageName, raw);
            }
        }

        // 2. Second pass: Launcher-only activities (for sideloaded apps without Leanback entry)
        for (RawActivity raw : rawActivities) {
            if (raw.isLeanback || raw.packageName.isEmpty() || raw.activityName.isEmpty()
                    || !raw.isExported || !raw.isEnabled || !raw.isResolvable
                    || raw.packageName.equals(selfPackageName)) {
                continue;
            }
            if (!unique.containsKey(raw.packageName)) {
                unique.put(raw.packageName, raw);
            }
        }

        List<RawActivity> result = new ArrayList<>(unique.values());
        java.text.Collator collator = java.text.Collator.getInstance();
        result.sort((a, b) -> {
            String pa = a.packageName, pb = b.packageName;
            boolean homeA = onHome != null && onHome.test(pa);
            boolean homeB = onHome != null && onHome.test(pb);
            long timeA = (installTimes != null && installTimes.containsKey(pa)) ? installTimes.get(pa) : 0L;
            long timeB = (installTimes != null && installTimes.containsKey(pb)) ? installTimes.get(pb) : 0L;
            int priority = LibraryApps.comparePriority(homeA, timeA, homeB, timeB, catalogTime);
            if (priority != 0) return priority;
            int byLabel = collator.compare(a.label, b.label);
            return byLabel != 0 ? byLabel : pa.compareTo(pb);
        });

        return Collections.unmodifiableList(result);
    }

    /**
     * Framework query execution for AppLibraryDialog.
     */
    public static List<ResolveInfo> discoverLibraryResolves(PackageManager pm,
                                                            String selfPackageName,
                                                            Set<String> outTvPackages,
                                                            java.util.function.Predicate<String> onHome,
                                                            Map<String, String> outLabels,
                                                            Map<String, Long> outInstallTimes) {
        Map<String, ResolveInfo> unique = new java.util.LinkedHashMap<>();
        if (outTvPackages != null) outTvPackages.clear();

        for (String category : new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER}) {
            List<ResolveInfo> queryResults = pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(category), 0);
            for (ResolveInfo info : queryResults) {
                if (info.activityInfo == null || !info.activityInfo.exported || !info.activityInfo.isEnabled()) continue;
                String pkg = info.activityInfo.packageName;
                if (Intent.CATEGORY_LEANBACK_LAUNCHER.equals(category) && outTvPackages != null) {
                    outTvPackages.add(pkg);
                }
                if (!pkg.equals(selfPackageName) && !unique.containsKey(pkg)) {
                    unique.put(pkg, info);
                }
            }
        }

        List<ResolveInfo> apps = new ArrayList<>(unique.values());
        if (outInstallTimes != null) outInstallTimes.clear();
        if (outLabels != null) outLabels.clear();
        Map<String, Boolean> homeMap = new HashMap<>();

        for (ResolveInfo app : apps) {
            String pkg = app.activityInfo.packageName;
            homeMap.put(pkg, onHome != null && onHome.test(pkg));
            String lbl = "";
            try {
                lbl = Cards.cleanLabel(String.valueOf(app.loadLabel(pm)), pkg);
            } catch (RuntimeException unavailable) {
                lbl = pkg;
            }
            if (outLabels != null) outLabels.put(pkg, lbl);

            long installTime = 0L;
            try {
                installTime = pm.getPackageInfo(pkg, 0).firstInstallTime;
            } catch (PackageManager.NameNotFoundException | RuntimeException ignored) {
            }
            if (outInstallTimes != null) outInstallTimes.put(pkg, installTime);
        }

        long catalogTime = System.currentTimeMillis();
        java.text.Collator collator = java.text.Collator.getInstance();
        apps.sort((a, b) -> {
            String pa = a.activityInfo.packageName, pb = b.activityInfo.packageName;
            boolean ha = homeMap.get(pa) != null && homeMap.get(pa);
            boolean hb = homeMap.get(pb) != null && homeMap.get(pb);
            long ta = (outInstallTimes != null && outInstallTimes.containsKey(pa)) ? outInstallTimes.get(pa) : 0L;
            long tb = (outInstallTimes != null && outInstallTimes.containsKey(pb)) ? outInstallTimes.get(pb) : 0L;
            int priority = LibraryApps.comparePriority(ha, ta, hb, tb, catalogTime);
            if (priority != 0) return priority;
            String la = outLabels != null ? outLabels.get(pa) : pa;
            String lb = outLabels != null ? outLabels.get(pb) : pb;
            int byLabel = collator.compare(la != null ? la : "", lb != null ? lb : "");
            return byLabel != 0 ? byLabel : pa.compareTo(pb);
        });

        return apps;
    }

    /**
     * Interface for resolving bundled drawables in AppAdapter.
     */
    public interface DrawableResolver {
        Drawable resolveBundledBanner(String bannerName);
    }
}
