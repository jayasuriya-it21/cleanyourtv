package com.gathin.tvhome;

import com.gathin.tvhome.config.ConfigPolicy;
import com.gathin.tvhome.content.demo.SyntheticContentFactory;
import com.gathin.tvhome.content.model.ContentItem;
import com.gathin.tvhome.content.model.ContentRow;
import com.gathin.tvhome.content.ui.ContentRowAdapter;
import com.gathin.tvhome.navigation.FocusMemory;
import com.gathin.tvhome.navigation.RowNavigationManager;
import com.gathin.tvhome.navigation.RowNavigationManager.UtilityCandidate;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests verifying Milestone 2.5 pre-device hardening:
 * - Pure Java configuration resolution (default false, runtime overlay true/false, malformed fallback)
 * - Focus memory stable-ID, missing-ID fallback, user-moved focus protection, and disabled-shelf clearing
 * - Top utility candidate evaluation (hidden controls, settings-only, tie handling, all-invalid fallback)
 * - Directional D-pad boundary absorption, event consumption, and repeated key safety
 */
public class ContentRowNavigationTest {

    // ---- Configuration Resolution Tests (Section 3) --------------------------------------------

    @Test
    public void configPolicy_defaultIsFalse() {
        assertFalse(ConfigPolicy.DEFAULT_SHOW_SYNTHETIC_SPOTLIGHT);
        assertFalse(ConfigPolicy.resolveSpotlightEnabled(ConfigPolicy.DEFAULT_SHOW_SYNTHETIC_SPOTLIGHT, (JSONObject) null));
        assertFalse(ConfigPolicy.resolveSpotlightEnabled(ConfigPolicy.DEFAULT_SHOW_SYNTHETIC_SPOTLIGHT, (String) null));
        assertFalse(ConfigPolicy.resolveSpotlightEnabled(ConfigPolicy.DEFAULT_SHOW_SYNTHETIC_SPOTLIGHT, ""));
    }

    @Test
    public void configPolicy_runtimeTrueEnablesFeature() {
        boolean enabled = ConfigPolicy.resolveSpotlightEnabled(false, "{\"show_synthetic_spotlight\": true}");
        assertTrue("Runtime JSON overlay with true must enable spotlight", enabled);
    }

    @Test
    public void configPolicy_runtimeFalseDisablesFeature() {
        boolean disabled = ConfigPolicy.resolveSpotlightEnabled(true, "{\"show_synthetic_spotlight\": false}");
        assertFalse("Runtime JSON overlay with false must disable spotlight", disabled);
    }

    @Test
    public void configPolicy_missingConfigurationUsesFalse() {
        boolean resolvedDefault = ConfigPolicy.resolveSpotlightEnabled(false, "{\"other_setting\": true}");
        assertFalse("Missing configuration key must fall back to false", resolvedDefault);

        boolean resolvedEmpty = ConfigPolicy.resolveSpotlightEnabled(false, "{}");
        assertFalse("Empty JSON object must fall back to false", resolvedEmpty);
    }

    @Test
    public void configPolicy_preferredParsedBooleanResolution() {
        // Preferred method directly receiving nullable Boolean
        assertTrue(ConfigPolicy.resolveSpotlightEnabled(false, Boolean.TRUE));
        assertFalse(ConfigPolicy.resolveSpotlightEnabled(true, Boolean.FALSE));
        assertFalse(ConfigPolicy.resolveSpotlightEnabled(false, (Boolean) null));
        assertTrue(ConfigPolicy.resolveSpotlightEnabled(true, (Boolean) null));
    }

    @Test
    public void configPolicy_failClosedDuplicateKeys() {
        // Duplicate keys must fail closed to compiled default
        boolean duplicate = ConfigPolicy.resolveSpotlightEnabled(false,
                "{\"show_synthetic_spotlight\": true, \"show_synthetic_spotlight\": false}");
        assertFalse("Duplicate key must fail closed to false", duplicate);
    }

    @Test
    public void configPolicy_failClosedNestedValues() {
        // Nested occurrences must not trigger top-level feature resolution
        boolean nested = ConfigPolicy.resolveSpotlightEnabled(false,
                "{\"theme\": {\"show_synthetic_spotlight\": true}}");
        assertFalse("Nested key must fail closed to false", nested);
    }

    @Test
    public void configPolicy_failClosedStringValues() {
        // Quoted strings "true" and "false" must not be treated as boolean literals
        boolean stringTrue = ConfigPolicy.resolveSpotlightEnabled(false,
                "{\"show_synthetic_spotlight\": \"true\"}");
        assertFalse("String 'true' must fail closed to compiled default", stringTrue);

        boolean stringFalse = ConfigPolicy.resolveSpotlightEnabled(true,
                "{\"show_synthetic_spotlight\": \"false\"}");
        assertTrue("String 'false' must fail closed to compiled default", stringFalse);
    }

    @Test
    public void configPolicy_failClosedNumericValues() {
        // Numbers 1 and 0 must fail closed to compiled default
        boolean numericOne = ConfigPolicy.resolveSpotlightEnabled(false,
                "{\"show_synthetic_spotlight\": 1}");
        assertFalse("Numeric 1 must fail closed to compiled default", numericOne);

        boolean numericZero = ConfigPolicy.resolveSpotlightEnabled(true,
                "{\"show_synthetic_spotlight\": 0}");
        assertTrue("Numeric 0 must fail closed to compiled default", numericZero);
    }

    @Test
    public void configPolicy_failClosedSimilarlyNamedKeys() {
        // Similarly named keys must not match
        boolean prefixKey = ConfigPolicy.resolveSpotlightEnabled(false,
                "{\"not_show_synthetic_spotlight\": true}");
        assertFalse("Prefix-extended key must fail closed to false", prefixKey);

        boolean suffixKey = ConfigPolicy.resolveSpotlightEnabled(false,
                "{\"show_synthetic_spotlight_v2\": true}");
        assertFalse("Suffix-extended key must fail closed to false", suffixKey);
    }

    // ---- Focus Restoration & Lifecycle Tests (Section 4) ---------------------------------------

    @Test
    public void focusRestoration_stableIdRestoration() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        FocusMemory memory = new FocusMemory();

        // Focus 3rd item (index 2: Marina Lights)
        ContentItem targetItem = items.get(2);
        memory.recordFocus(2, targetItem.id);

        assertEquals(2, memory.getLastFocusedIndex());
        assertEquals(targetItem.id, memory.getLastFocusedId());
        assertTrue(memory.hasRecordedFocus());

        // Restore against current items
        int restored = memory.resolveRestoration(true, items, false);
        assertEquals(2, restored);
    }

    @Test
    public void focusRestoration_missingIdFallback() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        FocusMemory memory = new FocusMemory();

        // Record an ID that does not exist in the list with index 4
        memory.recordFocus(4, "missing_synthetic_id");

        // Should fallback to clamped recorded index (4)
        int restored = memory.resolveRestoration(true, items, false);
        assertEquals(4, restored);
    }

    @Test
    public void focusRestoration_emptyRowFallback() {
        FocusMemory memory = new FocusMemory();
        memory.recordFocus(2, "some_id");

        // Null items list
        assertEquals(-1, memory.resolveRestoration(true, null, false));
        // Empty items list
        assertEquals(-1, memory.resolveRestoration(true, Collections.emptyList(), false));
    }

    @Test
    public void focusRestoration_featureDisabledBehavior() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        FocusMemory memory = new FocusMemory();
        memory.recordFocus(3, items.get(3).id);
        assertTrue(memory.hasRecordedFocus());

        // When feature is disabled, resolveRestoration must clear memory and return -1
        int restored = memory.resolveRestoration(false, items, false);
        assertEquals(-1, restored);
        assertFalse(memory.hasRecordedFocus());
        assertEquals(0, memory.getLastFocusedIndex());
        assertEquals("", memory.getLastFocusedId());
    }

    @Test
    public void focusRestoration_staleAdapterPosition() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        FocusMemory memory = new FocusMemory();

        // Stale index out of bounds with missing ID
        memory.recordFocus(99, "deleted_id");
        int restored = memory.resolveRestoration(true, items, false);
        // Clamps to items.size() - 1 (5)
        assertEquals(items.size() - 1, restored);
    }

    @Test
    public void focusRestoration_userMovedFocusBeforeRestorationExecutes() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        FocusMemory memory = new FocusMemory();
        memory.recordFocus(1, items.get(1).id);

        // When user moved focus to another view before deferred restoration executed,
        // restoration must return -1 and shouldRestoreFocus must return false
        assertFalse(memory.shouldRestoreFocus(true, true, false));
        assertEquals(-1, memory.resolveRestoration(true, items, true));

        // When activity is finishing, restoration must also be rejected
        assertFalse(memory.shouldRestoreFocus(true, false, true));
    }

    // ---- Top Utility Candidate Tests (Section 5) ------------------------------------------------

    @Test
    public void utilityCandidates_allControlsVisible() {
        List<UtilityCandidate> candidates = Arrays.asList(
                UtilityCandidate.valid(0, 100f), // Inputs
                UtilityCandidate.valid(1, 200f), // Wifi
                UtilityCandidate.valid(2, 300f), // Reorder
                UtilityCandidate.valid(3, 400f)  // Settings
        );

        // Poster at 190f should select Wifi (index 1, center 200f)
        assertEquals(1, RowNavigationManager.selectBestUtilityTarget(190f, candidates));

        // Poster at 390f should select Settings (index 3, center 400f)
        assertEquals(3, RowNavigationManager.selectBestUtilityTarget(390f, candidates));

        // Poster at 105f should select Inputs (index 0, center 100f)
        assertEquals(0, RowNavigationManager.selectBestUtilityTarget(105f, candidates));
    }

    @Test
    public void utilityCandidates_inputsHidden() {
        List<UtilityCandidate> candidates = Arrays.asList(
                UtilityCandidate.invalid(0),     // Inputs hidden
                UtilityCandidate.valid(1, 200f), // Wifi
                UtilityCandidate.valid(2, 300f), // Reorder
                UtilityCandidate.valid(3, 400f)  // Settings
        );

        // Poster at 80f would normally prefer Inputs (100f), but Inputs is invalid -> selects Wifi (200f)
        assertEquals(1, RowNavigationManager.selectBestUtilityTarget(80f, candidates));
    }

    @Test
    public void utilityCandidates_wifiHidden() {
        List<UtilityCandidate> candidates = Arrays.asList(
                UtilityCandidate.valid(0, 100f), // Inputs
                UtilityCandidate.invalid(1),     // Wifi hidden
                UtilityCandidate.valid(2, 300f), // Reorder
                UtilityCandidate.valid(3, 400f)  // Settings
        );

        // Poster at 210f: distance to Inputs (100) is 110, distance to Reorder (300) is 90 -> selects Reorder (index 2)
        assertEquals(2, RowNavigationManager.selectBestUtilityTarget(210f, candidates));
    }

    @Test
    public void utilityCandidates_reorderHidden() {
        List<UtilityCandidate> candidates = Arrays.asList(
                UtilityCandidate.valid(0, 100f), // Inputs
                UtilityCandidate.valid(1, 200f), // Wifi
                UtilityCandidate.invalid(2),     // Reorder hidden
                UtilityCandidate.valid(3, 400f)  // Settings
        );

        // Poster at 290f: distance to Wifi (200) is 90, distance to Settings (400) is 110 -> selects Wifi (index 1)
        assertEquals(1, RowNavigationManager.selectBestUtilityTarget(290f, candidates));
    }

    @Test
    public void utilityCandidates_settingsOnly() {
        List<UtilityCandidate> candidates = Arrays.asList(
                UtilityCandidate.invalid(0), // Inputs invalid
                UtilityCandidate.invalid(1), // Wifi invalid
                UtilityCandidate.invalid(2), // Reorder invalid
                UtilityCandidate.valid(3, 400f) // Only Settings valid
        );

        // Any poster X selects the only valid candidate (Settings, index 3)
        assertEquals(3, RowNavigationManager.selectBestUtilityTarget(50f, candidates));
        assertEquals(3, RowNavigationManager.selectBestUtilityTarget(500f, candidates));
    }

    @Test
    public void utilityCandidates_allControlsInvalid() {
        List<UtilityCandidate> candidates = Arrays.asList(
                UtilityCandidate.invalid(0),
                UtilityCandidate.invalid(1),
                UtilityCandidate.invalid(2),
                UtilityCandidate.invalid(3)
        );

        // When no utility control is valid, returns -1 (focus stays on poster)
        assertEquals(-1, RowNavigationManager.selectBestUtilityTarget(200f, candidates));
        assertEquals(-1, RowNavigationManager.selectBestUtilityTargetProportional(2, 6, candidates));
    }

    @Test
    public void utilityCandidates_nearestCenterTieHandling() {
        List<UtilityCandidate> candidates = Arrays.asList(
                UtilityCandidate.valid(0, 100f),
                UtilityCandidate.valid(1, 300f)
        );

        // Poster at 200f is exactly equidistant (|200-100| = 100, |200-300| = 100)
        // Deterministic tie-breaking must choose the lower index (0, leftmost)
        assertEquals(0, RowNavigationManager.selectBestUtilityTarget(200f, candidates));
    }

    // ---- DPAD Event Consumption & Boundary Tests (Section 6) -----------------------------------

    @Test
    public void firstItemLeftBoundary_staysAtBoundary() {
        int itemCount = 6;
        int currentPos = 0;

        // DPAD_LEFT at index 0 in LTR is absorbed
        assertTrue(RowNavigationManager.isHorizontalBoundary(currentPos, itemCount, true, false));
        int target = RowNavigationManager.handleHorizontalKey(currentPos, itemCount, true, false);
        assertEquals(0, target);
    }

    @Test
    public void lastItemRightBoundary_staysAtBoundary() {
        int itemCount = 6;
        int currentPos = 5;

        // DPAD_RIGHT at last index (5) in LTR is absorbed
        assertTrue(RowNavigationManager.isHorizontalBoundary(currentPos, itemCount, false, false));
        int target = RowNavigationManager.handleHorizontalKey(currentPos, itemCount, false, false);
        assertEquals(5, target);
    }

    @Test
    public void dpad_handledHorizontalMovements() {
        int itemCount = 6;

        // Move right from 0 -> 1
        assertEquals(1, RowNavigationManager.handleHorizontalKey(0, itemCount, false, false));
        // Move left from 3 -> 2
        assertEquals(2, RowNavigationManager.handleHorizontalKey(3, itemCount, true, false));

        // In RTL: move right is move previous, move left is move next
        assertEquals(0, RowNavigationManager.handleHorizontalKey(1, itemCount, false, true));
        assertEquals(2, RowNavigationManager.handleHorizontalKey(1, itemCount, true, true));
    }

    @Test
    public void dpad_repeatedInputAtBoundaries() {
        int itemCount = 6;
        // Simulating repeated DPAD_LEFT at start: consistently absorbs and never escapes or wraps
        for (int repeat = 0; repeat < 5; repeat++) {
            assertTrue(RowNavigationManager.isHorizontalBoundary(0, itemCount, true, false));
            assertEquals(0, RowNavigationManager.handleHorizontalKey(0, itemCount, true, false));
        }

        // Simulating repeated DPAD_RIGHT at end: consistently absorbs and never escapes or wraps
        for (int repeat = 0; repeat < 5; repeat++) {
            assertTrue(RowNavigationManager.isHorizontalBoundary(5, itemCount, false, false));
            assertEquals(5, RowNavigationManager.handleHorizontalKey(5, itemCount, false, false));
        }
    }

    @Test
    public void dpad_unavailableDockTarget() {
        // Empty dock coordinates
        assertEquals(0, RowNavigationManager.calculateDownTargetGeometric(100f, new float[0]));
        assertEquals(0, RowNavigationManager.calculateDownTargetGeometric(100f, null));

        // Proportional mapping with 0 dock items
        assertEquals(0, RowNavigationManager.calculateDownTargetProportional(2, 6, 0, 4));
    }

    @Test
    public void dpad_emptyAdapterSafety() {
        assertEquals(-1, RowNavigationManager.handleHorizontalKey(0, 0, true, false));
        assertEquals(-1, RowNavigationManager.handleHorizontalKey(0, 0, false, false));
        assertTrue(RowNavigationManager.isHorizontalBoundary(0, 0, true, false));
        assertEquals(-1, RowNavigationManager.calculateUpTarget(0, 4, 0, 0, false));
    }

    @Test
    public void dpad_staleAdapterPositionClamped() {
        int itemCount = 6;
        // Position 10 out of bounds is clamped to boundary
        assertEquals(5, RowNavigationManager.handleHorizontalKey(10, itemCount, false, false));
    }

    @Test
    public void deterministicDownTargetSelection() {
        float[] dockCenters = new float[]{100f, 300f, 500f, 700f};

        assertEquals(0, RowNavigationManager.calculateDownTargetGeometric(120f, dockCenters));
        assertEquals(2, RowNavigationManager.calculateDownTargetGeometric(480f, dockCenters));
        assertEquals(3, RowNavigationManager.calculateDownTargetGeometric(750f, dockCenters));

        int posterCount = 6;
        int dockCount = 4;
        int columns = 4;

        assertEquals(0, RowNavigationManager.calculateDownTargetProportional(0, posterCount, dockCount, columns));
        assertEquals(3, RowNavigationManager.calculateDownTargetProportional(5, posterCount, dockCount, columns));
        int midTarget = RowNavigationManager.calculateDownTargetProportional(2, posterCount, dockCount, columns);
        assertTrue(midTarget >= 0 && midTarget < dockCount);
    }

    @Test
    public void deterministicUpRestoration() {
        int posterCount = 6;
        int columns = 4;

        // When focus originated from spotlight, return exactly to last-focused poster
        int targetOriginated = RowNavigationManager.calculateUpTarget(1, columns, posterCount, 4, true);
        assertEquals(4, targetOriginated);

        // When focus did NOT originate from spotlight, map column proportionally
        assertEquals(0, RowNavigationManager.calculateUpTarget(0, columns, posterCount, 0, false));
        assertEquals(5, RowNavigationManager.calculateUpTarget(3, columns, posterCount, 0, false));
    }

    // ---- Domain Model Safety & Bundle Checks (Section 8) ---------------------------------------

    @Test
    public void syntheticRowFactory_returnsStableIds() {
        List<ContentItem> itemsRun1 = SyntheticContentFactory.createSpotlightItems(null);
        List<ContentItem> itemsRun2 = SyntheticContentFactory.createSpotlightItems(null);

        assertEquals(itemsRun1.size(), itemsRun2.size());
        for (int i = 0; i < itemsRun1.size(); i++) {
            assertEquals(itemsRun1.get(i).id, itemsRun2.get(i).id);
            assertTrue(itemsRun1.get(i).id.startsWith("synthetic_spotlight_"));
        }
    }

    @Test
    public void syntheticRowFactory_correctItemCount() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        assertEquals(SyntheticContentFactory.ITEM_COUNT, items.size());
        assertEquals(6, items.size());

        ContentRow row = SyntheticContentFactory.createSpotlightRow(null);
        assertEquals(6, row.size());
        assertFalse(row.isEmpty());
        assertTrue(row.isOfflineCached);
        assertFalse(row.requiresNetwork);
    }

    @Test
    public void syntheticItems_haveNoProviderIdOrDeepLink() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        for (ContentItem item : items) {
            assertNotNull(item.identity);
            assertEquals("", item.identity.providerId);
            assertEquals("", item.identity.candidateDeepLinkUri);
            assertEquals("", item.identity.providerContentId);
            assertNull(item.progress);
        }
    }

    @Test
    public void syntheticItems_noDuplicateSyntheticIds() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        Set<String> ids = new HashSet<>();
        for (ContentItem item : items) {
            boolean added = ids.add(item.id);
            assertTrue("Duplicate synthetic ID detected: " + item.id, added);
        }
        assertEquals(items.size(), ids.size());
    }

    @Test
    public void adapterStableIdMapping_consistentAndUnique() {
        List<ContentItem> items = SyntheticContentFactory.createSpotlightItems(null);
        Set<Long> stableIds = new HashSet<>();

        for (int i = 0; i < items.size(); i++) {
            ContentItem item = items.get(i);
            long id1 = ContentRowAdapter.stableId(item.id);
            long id2 = ContentRowAdapter.stableId(item.id);
            assertEquals("Stable ID must be deterministic", id1, id2);
            assertTrue("Stable ID must be positive", id1 > 0);

            boolean added = stableIds.add(id1);
            assertTrue("Stable IDs must be unique across items", added);
        }

        assertNotEquals(ContentRowAdapter.stableId("item_a"), ContentRowAdapter.stableId("item_b"));
    }
}
