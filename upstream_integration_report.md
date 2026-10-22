# Upstream Integration Report

**Integration Date:** 2026-10-22
**Target Upstream Repository:** `https://github.com/Gathin23/cleanyourtv.git`
**Origin Repository:** `https://github.com/jayasuriya-it21/cleanyourtv.git`
**Integration Status:** ALL QUALITY GATES PASSED â€” CONFLICTS RESOLVED â€” READY FOR INTEGRATION COMMIT

---

## 1. Branch and Commit Identifiers

| Field | Value |
| :--- | :--- |
| **Integration Branch** | `integration/upstream-20261022` |
| **Upstream Commit** | `4c55533a47012eb9520199b801e8f1245b21cff3` (`README: counts and card after Round O's second batch`) |
| **Customization Commit Before Integration** | `3580ac09576cde720caa7e4668fb3170f4e6e79a` (`docs: finalize pre-device freeze and hardware acceptance plan`) |
| **Common Merge Base** | `66b7214a1d876279db6cb61aa5c3ccfd2d8b5eb2` |
| **Total Upstream Commits Merged** | **615 commits** |

---

## 2. Merge Conflicts Encountered & Intentional Resolutions

During the non-final merge (`git merge --no-commit --no-ff upstream/main`), exactly 5 files had merge conflicts. Every conflict was resolved intentionally preserving both customization logic and upstream bug fixes:

| File | Classification | Resolution Description |
| :--- | :--- | :--- |
| `AGENTS.md` | Documentation / Project Rules | **Combined both sections**: Placed repository-specific project rules (safety, Java 17 baseline, D-pad remote-first, physical TV gates) at the top, followed by upstream CleanYourTV contributing and agent rules below a clean section divider. |
| `Config.java` | Configuration / Feature Toggles | **Integrated typed toggle with upstream parameters**: Added custom `boolean showSyntheticSpotlight` alongside all 20+ new upstream configuration fields in the constructor and `load(...)` method. Maintained delegation to `ConfigPolicy.resolveSpotlightEnabled(...)`. |
| `MainActivity.java` | UI Architecture & Navigation | **Preserved both spotlight and top-bar enhancements**: Retained `btnSettings` reference and all custom D-pad spotlight navigation methods (`handleSpotlightKey`, `handleSpotlightUp`, `isValidUtilityTarget`, `focusSpotlightPoster`, `focusSpotlightFromDock`, `calculateDockTargetFromPoster`). Incorporated upstream `wifiIcon`, `pillsShown` caching, and `firstTopButton()` navigation. |
| `AppAdapter.java` | App Discovery & Tile Building | **Reconciled reload signature with `AppDiscoveryManager`**: Updated `void reload(List<Cards.Card> cards, List<ResolveInfo> leanback)` to accept upstream's `leanback` parameter while continuing to use `AppDiscoveryManager.discoverHomeApps(...)` for clean separation and testability. |
| `AppLibraryDialog.java` | App Library Performance | **Adopted upstream off-thread loader**: Embraced upstream's asynchronous `LOADER.execute(...)` and `LibraryLoads` caching mechanism to avoid blocking the main UI thread when opening the full application catalog. |

---

## 3. Preservation of Custom Features vs. Upstream Fixes

### Customized Features Preserved
- **Synthetic Spotlight Shelf**: Prototype horizontal content row (`SyntheticContentFactory`, `ContentRowAdapter`, `ContentItem`, `Artwork`, `ContentRow`).
- **Unbreakable D-Pad Navigation**: Two-way geometric and proportional navigation between top bar, spotlight row, and dock (`RowNavigationManager`, `FocusMemory`).
- **Configuration Boundary**: Separation of JSON parsing in `Config.java` from typed policy evaluation in `ConfigPolicy.java`.
- **Pre-Device Hardening & Documentation**: All 7 documentation files, freeze reports, and physical TV acceptance checklist remain completely intact.
- **Prototype Disabled by Default**: `show_synthetic_spotlight` remains `false` by default in `config.xml`.

### Upstream Fixes & Enhancements Incorporated
- Asynchronous application library loading (`LibraryLoads`).
- Screensaver and idle clock enhancements (`ClockActivity`, `ClockScene`).
- Ambient glow toggling and smooth focus glow rendering (`ambient_glow`, `app_glow`).
- Top menu folding (collapsing Inputs, Wi-Fi, and Settings into a single gear).
- Support for 30+ new TV models, debloat lists, and device quirks.
- New benchmarking and verification tools (`tools/bench.py`, `tools/check.py`, `tools/privacy.py`).
- 8 new unit test suites with 53 additional test methods.

---

## 4. Quality Gate Results

All commands executed on `integration/upstream-20261022` and parsed directly from generated reports:

| Metric | Frozen Baseline (M2.5 + Freeze Docs) | Integration Branch Result | Status / Delta |
| :--- | :---: | :---: | :---: |
| **Whitespace check (`git diff --check`)** | 0 errors | **0 errors (Exit code: 0)** | **PASS** |
| **Unit test suites** | 18 suites | **26 suites** | **+8 suites** (all upstream suites passed) |
| **Total unit test methods** | 179 tests | **232 tests** | **+53 tests** (all passed) |
| **Test failures** | 0 | **0** | **PASS** |
| **Test errors** | 0 | **0** | **PASS** |
| **Test skipped** | 0 | **0** | **PASS** |
| **Lint errors** | 0 | **0** | **PASS** |
| **Lint warnings** | 62 | **75** | **+13 warnings** (pre-existing in upstream 2.6) |
| **Lint informational** | 80 | **97** | **+17 informational** |
| **Debug APK size** | 1,956,522 bytes (1,910.67 KB) | **2,037,157 bytes (1,989.41 KB)** | **+80,635 bytes** (+4.12% for 615 commits) |

---

## 5. Security, Manifest, and Platform Audit

| Category | Finding / Audit Result |
| :--- | :--- |
| **Network Permission** | **NO `android.permission.INTERNET`**. Launcher remains strictly offline and private. |
| **New Permissions** | `android.permission.WRITE_SECURE_SETTINGS` (for `ServiceRepair` to restore remote accessibility helpers after force stop; requires explicit ADB grant); `yandex.tv.permission.ACCESS_TV_SOURCE` (protection level normal, for YaOS source switching). |
| **Platform Target** | `minSdk` remains 26; `targetSdk` remains 34; `compileSdk` remains 34; Java 17 baseline preserved. |
| **Application ID** | `com.gathin.tvhome` preserved. |
| **Stock Launcher Recovery** | Intact. Upstream adds `stock_home_redirect` to assist firmware that forces stock launcher restoration. |
| **Secrets / Credentials** | Zero secrets, private keys, or machine-specific paths committed. |

---

## 6. Physical-TV Retest Requirements

- **Retest Status:** **MANDATORY upon physical device delivery.**
- **Reason:** The integration of 615 upstream commits brings new animation pathways, idle screensaver triggers, and remote key handling enhancements. While all 232 automated tests pass cleanly in JVM simulation, integrated tactile latency, D-pad focus bounce, and stock launcher recovery must be re-verified on the physical TCL 55T6D per `physical_tv_acceptance_checklist.md`.
- **Milestone 3 Gate:** Remains strictly **BLOCKED / NO-GO**.

---

## 7. Recommendation

**RECOMMENDATION: GO FOR INTEGRATION COMMIT**

All quality gates have passed. The integration branch `integration/upstream-20261022` is ready to be committed and merged into `feature/unified-content-launcher`.
