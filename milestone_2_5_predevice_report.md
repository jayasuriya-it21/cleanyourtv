# Milestone 2.5 Pre-Device Hardening Report

**Project**: CleanYourTV Content Hub Launcher (`com.gathin.tvhome`)
**Target Device**: TCL 55T6D (Google TV, MediaTek MT5895 SoC profile, ~1 GB RAM profile)
**Date**: October 21, 2026
**Status**: AUTOMATED QUALITY GATES PASSED; PHYSICAL ACCEPTANCE PENDING
**Branch**: `feature/unified-content-launcher`

---

## 1. Why Physical Validation is Unavailable

The TCL 55T6D Google TV unit has been ordered by the project owner but has **not arrived yet**.

At this stage:
- There is no physical TV hardware present.
- There is no local TV IP address.
- There is no ADB RSA authorization.
- No device connections, package modifications, launcher-role changes, screensaver activations, debloating routines, or hardware performance measurements could or should be executed.
- All hardware-dependent milestones remain strictly **BLOCKED** and pending device arrival.

---

## 2. Corrected Milestone 2 Claims

The Milestone 2 completion report (`milestone_2_report.md`) has been fully revised to align strictly with factual, verified evidence:

1. **Status**: Changed to `AUTOMATED QUALITY GATES PASSED; PHYSICAL ACCEPTANCE PENDING`.
2. **Regression Claims**: Removed claims of "zero regressions"; replaced with the factual statement that "no automated test, compilation, or lint-error regression was found."
3. **Android Focus Integration**: Explicitly noted that integrated Android remote focus behavior on real hardware has not been physically verified.
4. **Physical Remote Behavior**: Explicitly noted that Bluetooth/IR remote key bounce, tactile responsiveness, and rapid directional input latency remain unverified.
5. **GlassView Transitions**: Explicitly noted that `GlassView` spring animation and render aesthetics remain physically unverified on the target panel.
6. **Performance & Memory**: Explicitly noted that startup time, frame drop statistics, and runtime memory consumption (PSS) remain unmeasured until device profiling.
7. **APK Size vs. Runtime Memory**: Separated APK size impact (+25 KB) from runtime memory footprint, clarifying that low APK size does not prove low runtime RAM consumption.
8. **Frame Rates**: Removed speculative 60 FPS claims.
9. **Formatting**:
   - Converted all links to repository-relative paths (removed all `file:///` URLs).
   - Placed navigation state diagrams in fenced `mermaid` blocks.
   - Placed JSON examples in fenced `json` blocks.
   - Enforced markdown blank lines around headers, tables, lists, and code blocks.

---

## 3. Lint Warning Attribution & Remediation

The original CleanYourTV baseline contained **62 warnings, 0 errors**.
Milestone 2 pre-hardening reported **70 warnings (+8), 0 errors**.

### Warning Comparison & Attribution

| Warning ID | Severity | File | Line | Changed in M2? | Category | Cause / Resolution |
| :--- | :---: | :--- | :---: | :---: | :---: | :--- |
| `DiscouragedApi` | Warning | `ContentRowAdapter.java` | 93 | Yes (new file) | New | `Resources.getIdentifier()` used for dynamic drawable lookup. **Resolved**: Replaced with static `resolvePosterDrawable(posterUrl)` mapping. |
| `UnusedResources` | Warning | `poster_bus_home.xml` | 2 | Yes (new file) | New | Flagged unused due to dynamic name lookup. **Resolved**: Statically referenced in `resolvePosterDrawable()`. |
| `UnusedResources` | Warning | `poster_chennai_rain.xml` | 2 | Yes (new file) | New | Flagged unused due to dynamic name lookup. **Resolved**: Statically referenced in `resolvePosterDrawable()`. |
| `UnusedResources` | Warning | `poster_marina_lights.xml` | 2 | Yes (new file) | New | Flagged unused due to dynamic name lookup. **Resolved**: Statically referenced in `resolvePosterDrawable()`. |
| `UnusedResources` | Warning | `poster_river_stars.xml` | 2 | Yes (new file) | New | Flagged unused due to dynamic name lookup. **Resolved**: Statically referenced in `resolvePosterDrawable()`. |
| `UnusedResources` | Warning | `poster_silent_temple.xml` | 2 | Yes (new file) | New | Flagged unused due to dynamic name lookup. **Resolved**: Statically referenced in `resolvePosterDrawable()`. |
| `UnusedResources` | Warning | `poster_southern_signal.xml` | 2 | Yes (new file) | New | Flagged unused due to dynamic name lookup. **Resolved**: Statically referenced in `resolvePosterDrawable()`. |
| `UselessParent` | Warning | `item_content_poster.xml` | 12 | Yes (new file) | New | Root `FrameLayout` redundantly wrapped a single vertical `LinearLayout`. **Resolved**: Flattened root view directly to `LinearLayout`. |

### Final Lint Results
- **Lint Errors**: **0**
- **Lint Warnings**: **62** (exact match to original repository baseline)
- **New Lint Warnings**: **0**

---

## 4. Default-Feature Gating (Opt-In Only)

To prevent unverified UI modifications from affecting stock launcher fallback or normal operation before physical hardware validation:

1. **Compiled Default**:
   In `launcher/app/src/main/res/values/config.xml`:
   ```xml
   <bool name="show_synthetic_spotlight">false</bool>
   ```
2. **Behavior When Disabled (`false`) (Local Logic/Source Verification)**:
   - `spotlightContainer` remains `View.GONE` in layout XML.
   - Consumes exactly 0 layout space.
   - Hidden spotlight child views cannot receive or request focus.
   - Top utility bar and app dock maintain 100% of original direct navigation bindings in `MainActivity.java`.
   - The app grid remains the normal primary Home target.
3. **Runtime Configuration Overlay Support (Local Logic/Source Verification)**:
   The feature can be enabled via local config overlay without rebuilding the APK:
   `/sdcard/Android/data/com.gathin.tvhome/files/config.json`:
   ```json
   {
     "show_synthetic_spotlight": true
   }
   ```
4. **Pure Java Configuration Resolution (Local Logic/Source Verification)**:
   Created `com.gathin.tvhome.config.ConfigPolicy`:
   - Preferred resolution receives an already-parsed nullable `Boolean` from `Config.java`.
   - Fallback raw string parser enforces strict fail-closed behavior on malformed, duplicate, nested, string, numeric, and similarly named values.
   - Verified via automated JUnit tests on the host JVM; physical execution remains pending device arrival.

---

## 5. Focus-Restoration Audit

The focus restoration lifecycle was inspected and hardened across all transition paths:

| Transition Event | Prior Risk / Behavior | Hardened Implementation in M2.5 |
| :--- | :--- | :--- |
| **External Activity Return** | Handled in `onResume()` | Restores stored Spotlight poster ID via `FocusMemory.resolveRestoration(...)` only if activity is not finishing and views are ready. |
| **Standard Android Dialog Dismissal** | Might not trigger `onResume()` | Dismiss listener chains `restoreFocus()` without unbounded polling. |
| **Closing AppLibraryDialog** | Custom popup dismiss | Explicit dismissal callback handles restoration to library pill or dock. |
| **Returning from Settings** | Settings Activity or glass popup | Deterministic restoration via `restoreFocus()`. |
| **Activity Stop / Start** | View references become detached | Focus restored after adapter and layout pass settle using `post()` with lifecycle checks. |
| **Activity Recreation** | Stored memory re-initialized | Stable item ID resolution falls back gracefully to clamped index, then 0. |
| **Missing Previously Focused Item** | Focus lost or NPE | Evaluates stable item ID; if missing, clamps to previous index; if row is empty, returns -1. |
| **Spotlight Disabled Post-Focus** | Stored focus attempts to focus GONE view | `FocusMemory.resolveRestoration(false, ...)` clears memory and falls back cleanly to app dock. |
| **User Moved Focus Before Execution** | Deferred runnable forces focus away from user | Checked in `restoreFocus()`: if user already focused another valid view (`current != null && current.isShown()`), deferred restoration is skipped. |

---

## 6. Top-Utility Target Selection Audit

When pressing DPAD_UP from Tamil Spotlight:
- A utility target candidate is evaluated and considered valid only if:
  - It is non-null.
  - It is attached to the window (`isAttachedToWindow()`).
  - Its visibility is `View.VISIBLE`.
  - It is actively shown (`isShown()`).
  - It is enabled (`isEnabled()`).
  - It is focusable (`isFocusable()`).
  - It has non-zero runtime laid-out width and height (`getWidth() > 0 && getHeight() > 0`).
- Views that are `GONE`, `INVISIBLE`, disabled, detached, non-focusable, or zero-size are strictly excluded.
- **Decision Engine**:
  - `RowNavigationManager.selectBestUtilityTarget(posterCenterX, candidates)` evaluates candidate horizontal centers against the poster center.
  - In an exact tie in distance, deterministic tie-breaking selects the leftmost (lower-index) candidate.
  - If no candidate is valid (e.g., all top controls hidden or zero-size), the UP action is **consumed**, focus stays safely on the active poster, and `requestFocus()` is never called on an invalid view.

---

## 7. DPAD Event-Consumption & Key Ownership Audit

Key path ownership is strictly divided across classes:

1. **`ContentRowAdapter`**:
   - Owns `itemView.setOnKeyListener`.
   - Filters strictly for `event.getAction() == KeyEvent.ACTION_DOWN`.
   - Forwards navigation keys to `listener.onPosterKey`.
   - Never consumes `ACTION_UP` twice.
   - Forwards click actions to `listener.onPosterClicked` via `OnClickListener` (`DPAD_CENTER` / `ENTER`).
   - Returns `false` for unhandled keys (BACK, HOME, volume, media, settings), allowing normal system propagation.
2. **`RowNavigationManager`**:
   - Pure decision engine: evaluates horizontal boundary absorption, geometric DOWN targeting, UP target selection, and candidate ranking.
3. **`MainActivity`**:
   - Owns `handleSpotlightKey`:
     - DPAD_LEFT at index 0 is consumed (`return true`, focus absorbed).
     - DPAD_RIGHT at last index is consumed (`return true`, focus absorbed).
     - Handled horizontal movement updates focus, plays navigation sound, and consumes event.
     - Handled UP transitions via `handleSpotlightUp` consume event. If no top target is valid, consumes event and keeps focus.
     - Handled DOWN transitions navigate to dock tile. If no dock items exist, consumes event and keeps focus.
     - Rapid repeated DPAD inputs (`repeatCount > 0`) maintain boundary absorption without runaway wrapping.

---

## 8. GlassView Source-Level Safety Audit

A source audit was performed on `GlassView.java` and `MainActivity.java`:

- **Card ID Isolation**: Poster views in `item_content_poster.xml` use `android:id="@+id/posterContainer"`, strictly avoiding the `R.id.card` identifier used by app grid tiles.
- **Automatic Fade-Out**: In `MainActivity.java`, the global focus listener triggers `hideGlass()` whenever focus leaves `R.id.card`:
  ```java
  getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener((oldF, newF) -> {
      boolean onTile = newF != null && newF.getId() == R.id.card;
      if (!onTile) hideGlass();
  });
  ```
  Entering Spotlight causes `GlassView` to cleanly fade out, preventing spring animation collision.
- **Automatic Resumption**: When leaving Spotlight DOWN to the app dock, `onTileFocused` receives the focused card view and animates `GlassView` into position.
- **Detached View Protection**: `onDestroy()` explicitly calls `glass.animate().cancel()` and cancels any pending spotlight callbacks.
- **Opt-Out Isolation**: When `showSyntheticSpotlight == false`, Spotlight is `View.GONE` and has zero effect on `GlassView`.
- **Physical Verification**: Visual aesthetics on the TCL 55T6D remain **PENDING PHYSICAL VERIFICATION**.

---

## 9. Domain Model Safety Audit

The domain models in `com.gathin.tvhome.content.model` were reviewed:
- `ContentItem`
- `ContentIdentity`
- `ContentRow`
- `Artwork`
- `WatchProgress`
- `UserWatchlistEntry`
- `ContentSourceResult`

### Safety Confirmations:
- **Pure POJOs**: Removed `implements Serializable` and `serialVersionUID`.
- **Zero Persistence Annotations**: No Room, JSON annotations, or database bindings.
- **Immutability**: All fields are `public final`.
- **Defensive Copying**: Collections are wrapped in `Collections.unmodifiableList(new ArrayList<>(...))`.
- **No Deep Link Synthesis**: `candidateDeepLinkUri` is strictly an empty placeholder string; no speculative schemes are constructed.
- **Future-Facing State**: `WatchProgress` and `UserWatchlistEntry` are confirmed to be decoupled, future-facing domain structures that do not execute or assert false progress claims at runtime.

---

## 10. Automated Test Results

Executed Gradle unit test suite via `.\gradlew.bat test`:

- **Total Test Methods**: **174** (+19 new tests in M2.5)
- **Total Test Suites**: **18**
- **Failures**: **0**
- **Errors**: **0**
- **Skipped**: **0**

All 32 tests in `ContentRowNavigationTest` passed, covering configuration resolution, stable-ID restoration, missing-ID fallbacks, top-utility candidates, tie handling, boundary absorption, repeat inputs, and domain model safety.

---

## 11. Lint Verification Results

Executed Android lint via `.\gradlew.bat lint`:

- **Lint Errors**: **0**
- **Lint Warnings**: **62**
- **Informational Issues**: **80**
- **Net Delta vs Baseline**: **0 new warnings** (all 8 warnings introduced in Milestone 2 were completely resolved).

---

## 12. APK Size Comparison

| Milestone Build | APK Size (Bytes) | Size (KB) | Delta vs Baseline | Delta vs Prior Milestone |
| :--- | :---: | :---: | :---: | :---: |
| **Original Baseline** | 1,914,559 | 1,869.69 KB | 0 | - |
| **Milestone 1** | 1,928,655 | 1,883.45 KB | +14,096 B (+0.74%) | 0 |
| **Milestone 2 (Pre-Hardening)** | 1,953,728 | 1,907.94 KB | +39,169 B (+2.05%) | +25,073 B |
| **Milestone 2.5 (Hardened)** | **1,955,886** | **1,910.05 KB** | **+41,327 B (+2.16%)** | **+2,158 B (+0.11%)** |

The hardened Milestone 2.5 build adds only **2,158 bytes** over Milestone 2 pre-hardening, while eliminating all 8 lint warnings, isolating configuration resolution, and securing navigation boundaries.

---

## 13. Modified and Created Files

### Created in Milestone 2.5
- `launcher/app/src/main/java/com/gathin/tvhome/config/ConfigPolicy.java`
- `physical_tv_acceptance_checklist.md`
- `milestone_2_5_predevice_report.md`

### Modified in Milestone 2.5
- `launcher/app/src/main/res/values/config.xml` (`show_synthetic_spotlight` set to `false`)
- `launcher/app/src/main/res/layout/item_content_poster.xml` (flattened root view)
- `launcher/app/src/main/java/com/gathin/tvhome/Config.java` (delegated spotlight toggle to `ConfigPolicy`)
- `launcher/app/src/main/java/com/gathin/tvhome/MainActivity.java` (hardened utility selection, key handling, and lifecycle cleanup)
- `launcher/app/src/main/java/com/gathin/tvhome/navigation/FocusMemory.java` (lifecycle and user-focus guards)
- `launcher/app/src/main/java/com/gathin/tvhome/navigation/RowNavigationManager.java` (utility candidate evaluation and ranking)
- `launcher/app/src/main/java/com/gathin/tvhome/content/model/Artwork.java` (removed `Serializable`)
- `launcher/app/src/main/java/com/gathin/tvhome/content/model/ContentIdentity.java` (removed `Serializable`)
- `launcher/app/src/main/java/com/gathin/tvhome/content/model/ContentItem.java` (removed `Serializable`)
- `launcher/app/src/main/java/com/gathin/tvhome/content/model/ContentRow.java` (removed `Serializable`)
- `launcher/app/src/main/java/com/gathin/tvhome/content/model/ContentSourceResult.java` (removed `Serializable`)
- `launcher/app/src/main/java/com/gathin/tvhome/content/model/UserWatchlistEntry.java` (removed `Serializable`)
- `launcher/app/src/main/java/com/gathin/tvhome/content/model/WatchProgress.java` (removed `Serializable`)
- `launcher/app/src/test/java/com/gathin/tvhome/ContentRowNavigationTest.java` (expanded to 32 unit tests)
- `milestone_2_report.md` (claims and formatting corrections)
- `task.md` (status tracking update)
- `implementation_plan.md` (roadmap gating update)

---

## 14. Remaining Risks

1. **Physical Remote Key Latency & Bounce**: Budget TV remotes can send spurious repeated scancodes or exhibit input latency that cannot be modeled in unit tests.
2. **MediaTek MT5895 GPU Scaling**: The visual appearance of 6 vector XML drawables at 4K resolution on a 55" display needs hardware confirmation for rendering smoothness.
3. **Runtime Memory Heap Overhead**: While APK size is well within budget (+41 KB total), real heap footprint under extended use must be verified via `dumpsys meminfo`.

---

## 15. Physical-TV Acceptance Checklist Location

The complete, ready-to-run physical hardware verification checklist has been created at:
`physical_tv_acceptance_checklist.md`

It covers Sections A through G:
- A. Initial Setup (stock baseline verification)
- B. Developer Options & Network Security
- C. Safe Non-Destructive Hardware Probe
- D. Safe Installation & Baseline Preservation
- E. Spotlight QA
- F. Hardware Measurement & Profiling
- G. Clean Rollback Procedure

---

## 16. Explicit NO-GO for Milestone 3 Until Device Acceptance

> [!CAUTION]
> **MILESTONE 3 IS STRICTLY BLOCKED (EXPLICIT NO-GO)**
> In accordance with project safety rules, **Milestone 3 (Multi-Row Home Container & Local Hero Showcase) MUST NOT BEGIN** until:
> 1. The ordered TCL 55T6D physically arrives.
> 2. The physical setup, ADB authorization, and non-destructive hardware probe are executed.
> 3. The 7-phase verification protocol in `physical_tv_acceptance_checklist.md` passes on physical hardware.
