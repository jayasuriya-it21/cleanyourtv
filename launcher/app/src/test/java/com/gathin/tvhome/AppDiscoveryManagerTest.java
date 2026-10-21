package com.gathin.tvhome;

import com.gathin.tvhome.apps.AppCategory;
import com.gathin.tvhome.apps.AppDiscoveryManager;
import com.gathin.tvhome.apps.AppDiscoveryManager.DiscoveryConfig;
import com.gathin.tvhome.apps.AppDiscoveryManager.RawActivity;
import com.gathin.tvhome.apps.InstalledApp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests validating pure normalization, filtering, Leanback priority, deduplication,
 * categorization, and ordering logic in AppDiscoveryManager.
 */
public class AppDiscoveryManagerTest {

    private static final String SELF_PKG = "com.gathin.tvhome";

    private DiscoveryConfig createDefaultConfig() {
        return new DiscoveryConfig(
                SELF_PKG,
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }

    @Test
    public void testLeanbackOnlyApp() {
        List<RawActivity> raw = Collections.singletonList(
                RawActivity.createSimple("com.netflix.ninja", "com.netflix.ninja.MainActivity", "Netflix", true, true, true)
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, createDefaultConfig());

        assertEquals(1, result.size());
        InstalledApp app = result.get(0);
        assertEquals("com.netflix.ninja", app.packageName);
        assertEquals("Netflix", app.label);
        assertEquals(AppCategory.TV_NATIVE, app.category);
        assertTrue(app.isTvNative());
        assertFalse(app.requiresTouchWarning());
    }

    @Test
    public void testLauncherOnlyAppExcludedUnlessAdded() {
        List<RawActivity> raw = Collections.singletonList(
                RawActivity.createSimple("org.phone.app", "org.phone.app.MainActivity", "PhoneApp", false, true, true)
        );

        // Not in libraryAdded -> excluded
        List<InstalledApp> resultNotAdded = AppDiscoveryManager.normalize(raw, createDefaultConfig());
        assertEquals(0, resultNotAdded.size());

        // In libraryAdded -> included and classified as touch
        DiscoveryConfig addedConfig = new DiscoveryConfig(
                SELF_PKG, Collections.emptySet(), Collections.emptyList(), Collections.emptyMap(),
                Collections.singleton("org.phone.app"), Collections.emptySet(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()
        );

        List<InstalledApp> resultAdded = AppDiscoveryManager.normalize(raw, addedConfig);
        assertEquals(1, resultAdded.size());
        InstalledApp app = resultAdded.get(0);
        assertEquals("org.phone.app", app.packageName);
        assertEquals(AppCategory.SIDELOADED_TOUCH, app.category);
        assertFalse(app.isTvNative());
        assertTrue(app.requiresTouchWarning());
    }

    @Test
    public void testPreferenceForLeanbackActivity() {
        // App declares both Leanback TV and phone Launcher activities
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.example.dual", "com.example.dual.PhoneActivity", "DualApp", false, true, true),
                RawActivity.createSimple("com.example.dual", "com.example.dual.TvActivity", "DualApp", true, true, true)
        );

        // Even if libraryAdded contains it, Leanback activity must be preferred and phone activity dropped
        DiscoveryConfig config = new DiscoveryConfig(
                SELF_PKG, Collections.emptySet(), Collections.emptyList(), Collections.emptyMap(),
                Collections.singleton("com.example.dual"), Collections.emptySet(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, config);
        assertEquals(1, result.size());
        InstalledApp app = result.get(0);
        assertEquals("com.example.dual.TvActivity", app.activityName);
        assertEquals(AppCategory.TV_NATIVE, app.category);
    }

    @Test
    public void testDuplicateComponentResults() {
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.app", "com.app.MainActivity", "App", true, true, true),
                RawActivity.createSimple("com.app", "com.app.MainActivity", "App", true, true, true)
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, createDefaultConfig());
        assertEquals(1, result.size());
    }

    @Test
    public void testDuplicatePackageAndLabelDifferentActivities() {
        // e.g. Sony Timers & Clock declaring 2 activities with the same label
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.sony.clock", "com.sony.clock.Act1", "Clock", true, true, true),
                RawActivity.createSimple("com.sony.clock", "com.sony.clock.Act2", "Clock", true, true, true)
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, createDefaultConfig());
        assertEquals(1, result.size());
        assertEquals("com.sony.clock.Act1", result.get(0).activityName);
    }

    @Test
    public void testDisabledActivityFiltered() {
        List<RawActivity> raw = Collections.singletonList(
                RawActivity.createSimple("com.disabled", "com.disabled.MainActivity", "Disabled", true, true, false)
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, createDefaultConfig());
        assertEquals(0, result.size());
    }

    @Test
    public void testUnexportedActivityFiltered() {
        List<RawActivity> raw = Collections.singletonList(
                RawActivity.createSimple("com.unexported", "com.unexported.MainActivity", "Unexported", true, false, true)
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, createDefaultConfig());
        assertEquals(0, result.size());
    }

    @Test
    public void testUnresolvableActivityFiltered() {
        RawActivity unresolvable = new RawActivity(
                "com.broken", "com.broken.MainActivity", "Broken", null, null, true, true, true, false
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(Collections.singletonList(unresolvable), createDefaultConfig());
        assertEquals(0, result.size());
    }

    @Test
    public void testHiddenAppFiltering() {
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.visible", "com.visible.MainActivity", "Visible", true, true, true),
                RawActivity.createSimple("com.hidden", "com.hidden.MainActivity", "Hidden", true, true, true)
        );

        DiscoveryConfig config = new DiscoveryConfig(
                SELF_PKG, Collections.singleton("com.hidden"), Collections.emptyList(),
                Collections.emptyMap(), Collections.emptySet(), Collections.emptySet(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, config);
        assertEquals(1, result.size());
        assertEquals("com.visible", result.get(0).packageName);
    }

    @Test
    public void testSelfPackageFiltering() {
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple(SELF_PKG, "com.gathin.tvhome.MainActivity", "Home", true, true, true),
                RawActivity.createSimple("com.other.app", "com.other.app.MainActivity", "Other", true, true, true)
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, createDefaultConfig());
        assertEquals(1, result.size());
        assertEquals("com.other.app", result.get(0).packageName);
    }

    @Test
    public void testDeterministicAlphabeticalFallback() {
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.c", "com.c.Main", "Zebra", true, true, true),
                RawActivity.createSimple("com.b", "com.b.Main", "Apple", true, true, true),
                RawActivity.createSimple("com.a", "com.a.Main", "Mango", true, true, true)
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, createDefaultConfig());
        assertEquals(3, result.size());
        assertEquals("Apple", result.get(0).label);
        assertEquals("Mango", result.get(1).label);
        assertEquals("Zebra", result.get(2).label);
    }

    @Test
    public void testDockOrderingPreserved() {
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.alpha", "com.alpha.Main", "Alpha", true, true, true),
                RawActivity.createSimple("com.youtube", "com.youtube.Main", "YouTube", true, true, true),
                RawActivity.createSimple("com.netflix", "com.netflix.Main", "Netflix", true, true, true)
        );

        // Dock favorites: Netflix first, then YouTube
        DiscoveryConfig config = new DiscoveryConfig(
                SELF_PKG, Collections.emptySet(), Arrays.asList("com.netflix", "com.youtube"),
                Collections.emptyMap(), Collections.emptySet(), Collections.emptySet(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, config);
        assertEquals(3, result.size());
        assertEquals("com.netflix", result.get(0).packageName);
        assertEquals("com.youtube", result.get(1).packageName);
        assertEquals("com.alpha", result.get(2).packageName);
    }

    @Test
    public void testCustomOrderPreserved() {
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.app.a", "com.app.a.Main", "AppA", true, true, true),
                RawActivity.createSimple("com.app.b", "com.app.b.Main", "AppB", true, true, true),
                RawActivity.createSimple("com.app.c", "com.app.c.Main", "AppC", true, true, true)
        );

        // User custom order gives AppC rank 0, AppA rank 1, AppB unranked
        Map<String, Integer> savedRanks = new HashMap<>();
        savedRanks.put("com.app.c/com.app.c.Main", 0);
        savedRanks.put("com.app.a/com.app.a.Main", 1);

        DiscoveryConfig config = new DiscoveryConfig(
                SELF_PKG, Collections.emptySet(), Collections.emptyList(),
                savedRanks, Collections.emptySet(), Collections.emptySet(),
                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap()
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, config);
        assertEquals(3, result.size());
        assertEquals("com.app.c", result.get(0).packageName);
        assertEquals("com.app.a", result.get(1).packageName);
        assertEquals("com.app.b", result.get(2).packageName);
    }

    @Test
    public void testCustomLabelOverride() {
        List<RawActivity> raw = Collections.singletonList(
                RawActivity.createSimple("com.app.original", "com.app.original.Main", "OriginalName", true, true, true)
        );

        Map<String, String> customLabels = Collections.singletonMap("com.app.original", "My Custom App");
        DiscoveryConfig config = new DiscoveryConfig(
                SELF_PKG, Collections.emptySet(), Collections.emptyList(),
                Collections.emptyMap(), Collections.emptySet(), Collections.emptySet(),
                customLabels, Collections.emptyMap(), Collections.emptyMap()
        );

        List<InstalledApp> result = AppDiscoveryManager.normalize(raw, config);
        assertEquals(1, result.size());
        assertEquals("My Custom App", result.get(0).label);
    }

    @Test
    public void testLibraryNormalizationLauncherOnlyAppDiscoverable() {
        // App has only a standard phone Launcher activity and is NOT on Home
        List<RawActivity> raw = Collections.singletonList(
                RawActivity.createSimple("com.phone.only", "com.phone.only.Main", "PhoneOnly", false, true, true)
        );
        Set<String> tvPackages = new HashSet<>();

        List<RawActivity> result = AppDiscoveryManager.normalizeLibrary(
                raw, SELF_PKG, pkg -> false, Collections.emptyMap(), System.currentTimeMillis(), tvPackages
        );

        assertEquals(1, result.size());
        assertEquals("com.phone.only", result.get(0).packageName);
        assertFalse(tvPackages.contains("com.phone.only"));
    }

    @Test
    public void testLibraryNormalizationDualCategoryPrefersLeanback() {
        // App declares both Leanback and Launcher activities
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.dual.app", "com.dual.app.PhoneActivity", "DualApp", false, true, true),
                RawActivity.createSimple("com.dual.app", "com.dual.app.TvActivity", "DualApp", true, true, true)
        );
        Set<String> tvPackages = new HashSet<>();

        List<RawActivity> result = AppDiscoveryManager.normalizeLibrary(
                raw, SELF_PKG, pkg -> false, Collections.emptyMap(), System.currentTimeMillis(), tvPackages
        );

        assertEquals(1, result.size());
        assertEquals("com.dual.app.TvActivity", result.get(0).activityName);
        assertTrue(tvPackages.contains("com.dual.app"));
    }

    @Test
    public void testLibraryNormalizationSelfPackageExcluded() {
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple(SELF_PKG, "com.gathin.tvhome.MainActivity", "Home", true, true, true),
                RawActivity.createSimple("com.other.app", "com.other.app.Main", "Other", true, true, true)
        );

        List<RawActivity> result = AppDiscoveryManager.normalizeLibrary(
                raw, SELF_PKG, pkg -> false, Collections.emptyMap(), System.currentTimeMillis(), null
        );

        assertEquals(1, result.size());
        assertEquals("com.other.app", result.get(0).packageName);
    }

    @Test
    public void testLibraryNormalizationPriorityOrdering() {
        // App A is on Home, App B is not on Home
        List<RawActivity> raw = Arrays.asList(
                RawActivity.createSimple("com.onhome", "com.onhome.Main", "OnHome", true, true, true),
                RawActivity.createSimple("com.nothome", "com.nothome.Main", "NotHome", true, true, true)
        );

        // App Library orders items NOT on Home first (to help user discover/add them)
        List<RawActivity> result = AppDiscoveryManager.normalizeLibrary(
                raw, SELF_PKG, pkg -> "com.onhome".equals(pkg), Collections.emptyMap(), System.currentTimeMillis(), null
        );

        assertEquals(2, result.size());
        assertEquals("com.nothome", result.get(0).packageName);
        assertEquals("com.onhome", result.get(1).packageName);
    }
}
