# Upstream Synchronization Analysis Report

**Document Version:** 1.0
**Analysis Date:** 2026-10-22
**Target Upstream Repository:** `https://github.com/Gathin23/cleanyourtv.git`
**Target Origin Repository:** `https://github.com/jayasuriya-it21/cleanyourtv.git`
**Customization Branch:** `feature/unified-content-launcher`

---

## 1. Remote and Branch Identification

| Parameter | Value |
| :--- | :--- |
| **Origin URL** | `https://github.com/jayasuriya-it21/cleanyourtv.git` |
| **Upstream URL** | `https://github.com/Gathin23/cleanyourtv.git` |
| **Upstream Default Branch** | `upstream/main` (discovered via `git remote show upstream`) |
| **Customization Branch** | `feature/unified-content-launcher` |
| **Current Local Checkpoint Commit** | `3580ac09576cde720caa7e4668fb3170f4e6e79a` (`docs: finalize pre-device freeze and hardware acceptance plan`) |
| **Current Upstream Commit** | `4c55533a47012eb9520199b801e8f1245b21cff3` (`README: counts and card after Round O's second batch`) |
| **Common Merge Base** | `66b7214a1d876279db6cb61aa5c3ccfd2d8b5eb2` (`README: Product Hunt badge says live now`) |

---

## 2. Commit Divergence Summary

| Direction | Commit Count | Description |
| :--- | :---: | :--- |
| **Upstream Only (`HEAD..upstream/main`)** | **615** | Official CleanYourTV releases and community enhancements |
| **Customization Only (`upstream/main..HEAD`)** | **2** | `8b0862b` (Milestone 2.5 code/hardening) + `3580ac0` (Pre-device freeze docs) |

### Commits Available Only in Customized Branch
1. `8b0862b`: `feat: pre-device freeze audit and synthetic spotlight hardening (Milestone 2.5)`
2. `3580ac0`: `docs: finalize pre-device freeze and hardware acceptance plan`

### Key Themes of Upstream Commits
- Release tag `v2.6.0` and intermediate maintenance rounds.
- Community debloat lists and device notes for 30+ new TV models.
- New launcher features: ambient glow toggle, Scenes hourly wallpapers, clock screensaver (`ClockScene`/`ClockActivity`), 32-bit wallpaper decoding (`wallpaper_true_color`), off-UI-thread app library loading (`LibraryLoads`), network pill toggling, and multi-language translations (Uzbek, Norwegian, Dutch, Spanish, Arabic, Hebrew, Italian, Indonesian).
- Tooling additions: `tools/check.py`, `tools/bench.py`, `tools/relay.py`, `tools/privacy.py`, screenshot capture in `tools/tv.py`.

---

## 3. Subsystem Impact Analysis

### 11. Upstream Changes Affecting `launcher/`
- **Core Activities & Dialogs**: `MainActivity.java`, `AppLibraryDialog.java`, `LauncherSettingsActivity.java`, `QuickSettingsActivity.java`, `InputsRequestActivity.java`.
- **View Hierarchy & Layouts**: `activity_main.xml`, `item_app.xml`, `item_card_row.xml`, `dream_clock.xml`.
- **Background Tasks & Loading**: Added `LibraryLoads.java` for asynchronous Leanback package discovery and icon loading. Added `TileRefresh.java` and `Motion.java` for entrance and focus transitions.
- **Visuals & Wallpapers**: Added `WallpaperCaptions.java`, `ClockScene.java`, and enhanced `GlassView.java` (smooth glow rings and color animators).
- **Resources & I18N**: Expanded strings, styles, and locales across 20+ language folders.

### 12. Upstream Changes Affecting `tools/`
- Added `tools/check.py` to detect configuration/package drift against device debloat lists.
- Added `tools/bench.py` for key latency, frame drops, and idle resource benchmarking.
- Added `tools/relay.py` for headless TV communication via network proxy.
- Added `tools/privacy.py` and enhanced `tools/tv.py` with screenshot pulling.
- Recovery scripts: Updated `tools/disable.sh` and `tools/disable.ps1` with input driver detection and safety guards.

### 13. Upstream Changes Affecting Build Scripts
- `launcher/app/build.gradle`: Updated versionCode/versionName, dependencies, and APK lint gates.
- `launcher/build-and-install.ps1` & `launcher/build-and-install.sh`: TCL `AUTO_START` app-op configuration and key service rebinding.

### 14. Upstream Changes Affecting Configuration
- `Config.java` added support for: `ambient_glow`, `show_network_pill`, `wallpaper_true_color`, `always_show_names`, `compact_top_bar`, `boot_to_live_tv`, `stock_home_redirect`, and `top_menu`.
- `config.xml`: Default values added for new features.

### 15. Upstream Changes Affecting Recovery Behavior
- Preservation of stock launcher recovery maintained upstream; additional checks for `stock_home_redirect` on firmware that forces default launcher restoration.

### 16. Upstream Changes Affecting Documentation
- Extensively expanded README, showcase, examples, and troubleshooting guides for new TV hardware profiles.

---

## 4. Expected Merge Conflict Areas

When merging `upstream/main` into the customized branch:

1. **`MainActivity.java` (HIGH RISK)**:
   - *Upstream*: Integrated `LibraryLoads`, `TileRefresh`, `ClockScene`, ambient glow, top menu collapse, and memory trim handlers.
   - *Custom*: Added `RowNavigationManager`, `FocusMemory`, `SyntheticContentFactory`, and Spotlight horizontal carousel bindings.
   - *Resolution Strategy*: Preserve both. Maintain content row navigation and GlassView hooks while integrating upstream off-thread library loading.

2. **`Config.java` (HIGH RISK)**:
   - *Upstream*: Added 10+ new boolean and integer configuration properties.
   - *Custom*: Added `showSyntheticSpotlight` with typed `Boolean` delegation to `ConfigPolicy.java`.
   - *Resolution Strategy*: Preserve all upstream configuration keys; retain `showSyntheticSpotlight` resolution logic.

3. **`activity_main.xml` (MEDIUM RISK)**:
   - *Upstream*: Restructured layout elements for compact top bar, network pill, and top menu.
   - *Custom*: Injected horizontal `RecyclerView` (`@+id/content_row_recycler`) for Spotlight showcase.
   - *Resolution Strategy*: Retain content row RecyclerView inside the vertical scroll container.

4. **`AppLibraryDialog.java` (MEDIUM RISK)**:
   - *Upstream*: Offloaded icon loading via `LibraryLoads`.
   - *Custom*: Refactored to leverage `AppDiscoveryManager` and category normalization.
   - *Resolution Strategy*: Reconcile discovery manager with asynchronous loading.

5. **`strings.xml` & `config.xml` (LOW RISK)**:
   - *Upstream*: New strings and feature toggles.
   - *Custom*: Added `show_synthetic_spotlight` toggle and content titles.
   - *Resolution Strategy*: Union of XML elements.

---

## 5. Risk Classification & Physical-TV Impact

| Category | Classification | Rationale |
| :--- | :---: | :--- |
| **Architecture Conflict Risk** | **HIGH** | Significant parallel evolution in `MainActivity.java` and `Config.java`. |
| **Security Risk** | **LOW** | Upstream remains zero-telemetry, zero-cloud, and local-only. |
| **Device Safety Risk** | **MEDIUM** | Upstream introduces `stock_home_redirect` and background memory trim logic that must be empirically verified on target TV. |
| **Physical-TV Retest Requirement** | **MANDATORY** | Merging 615 upstream commits introduces new runtime animations, memory trimming, and key services. Integrated hardware verification on the physical TCL 55T6D will be required. |

---

## 6. Recommended Integration Procedure

1. **Isolated Integration Branch**: Perform the merge on `integration/upstream-YYYYMMDD` branched from `feature/unified-content-launcher`.
2. **Non-Final Merge**: Execute `git merge --no-commit --no-ff upstream/main`.
3. **Manual Conflict Resolution**: Resolve each file with two-way analysis (preserve upstream bug fixes and preserve launcher customization).
4. **Local Quality Gates**: Validate using `git diff --check`, `gradlew test`, `gradlew lint`, and `gradlew assembleDebug`.
5. **Freeze Verification**: Ensure prototype spotlight remains disabled by default (`show_synthetic_spotlight: false`) and Milestone 3 remains blocked.
6. **Merge to Customization**: Merge integration branch into `feature/unified-content-launcher` only after all gates pass.

---

*Report generated for pre-merge upstream evaluation.*
