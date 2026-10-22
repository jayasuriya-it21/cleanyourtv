# CleanYourTV Content Hub: Implementation Plan

## Executive Summary

This implementation plan establishes a low-risk, phased roadmap for transforming `com.gathin.tvhome` into a unified Android TV content hub. Work proceeds through strictly verified milestones, prioritizing baseline stability, no automated regression detected between milestones, and local-first execution.

All physical TV validation and hardware testing on the TCL 55T6D remain **PENDING PHYSICAL DEVICE ARRIVAL**. Milestone 3 is strictly **BLOCKED** and must not begin until physical hardware acceptance of Milestone 2 / 2.5 is completed on the physical TV.

---

## Phased Milestones

### Milestone 1: Domain Foundation & App Discovery Extraction (Completed)
- **Scope**:
  - Stable domain models in `com.gathin.tvhome.content.model` and `com.gathin.tvhome.apps`.
  - Disambiguated `ContentIdentity` separating canonical metadata IDs from provider IDs.
  - Creation of `AppDiscoveryManager` extracting discovery logic from `AppAdapter`, `AppLibraryDialog`, and `LibraryApps`.
  - Resolution of Leanback vs. Launcher intents, deduplication by component/package, unexported/disabled activity filtering, and phone-only classification.
  - Minimal compatibility bridge in `AppAdapter` preserving 100% of existing visual and remote interaction behavior.
  - Pure unit tests for normalization, deduplication, and ordering without heavy mocking.
- **Verification Gates**:
  - `gradlew test`: 142 passed across 17 suites (0 failures, 0 errors).
  - `gradlew lint`: 0 errors, 62 baseline warnings.
  - `gradlew assembleDebug`: 1,928,655 bytes (+14,096 bytes vs baseline).

### Milestone 2: Synthetic Content Row & D-Pad Navigation Engine
- **Automated Implementation (Completed)**:
  - Single horizontal `ContentRow` rendering synthetic local content items (Tamil Spotlight).
  - Integration above existing Dock and Grid in `MainActivity`.
  - Deterministic D-pad navigation engine (`RowNavigationManager`) with boundary absorption.
  - Focus restoration engine (`FocusMemory`) tracking stable item IDs.
  - Dedicated poster card focus treatment avoiding collision with `GlassView`.
  - Zero network, zero remote images.
  - 155 automated unit tests passing across 18 test suites.
- **Physical Verification (PENDING)**:
  - Physical D-pad remote responsiveness, tactile latency, and key bounce.
  - Cold/warm start latency and runtime memory (PSS) profiling on the physical SoC (unknown pending hardware probe).
  - Blocked pending physical arrival of ordered TCL 55T6D.

### Milestone 2.5: Pre-Device Hardening & Prototype Gating (Completed Locally)
- **Scope**:
  - Corrected Milestone 2 claims in `milestone_2_report.md` (status: `AUTOMATED QUALITY GATES PASSED; PHYSICAL ACCEPTANCE PENDING`).
  - Comprehensive lint warning audit: resolved dynamic lookup and layout nesting warnings; warning count returned to exact baseline of 62 warnings (0 new warnings, 0 errors).
  - Disabled prototype by default: `show_synthetic_spotlight` set to `false` in compiled `config.xml` (row is `View.GONE`, consumes 0 layout space, children cannot receive focus).
  - Pure Java `ConfigPolicy` for runtime JSON overlay resolution (`show_synthetic_spotlight: true`).
  - Audited top-utility target selection: candidate ranking engine in `RowNavigationManager`, strict runtime validation (attached, visible, enabled, focusable, non-zero size), and safe absorption when no target is valid.
  - Focus restoration lifecycle hardening: guards against overriding user focus, activity destruction cleanup, feature-disabled state clearing.
  - DPAD event consumption audit: strict boundary absorption, repeat key support, and safe passthrough for unhandled keys.
  - GlassView safety audit: confirmed ID isolation (`posterContainer` vs `card`), focus change listener dismissal, and destruction cleanup.
  - Domain model safety audit: removed `Serializable`, confirmed pure immutable records.
  - Created `physical_tv_acceptance_checklist.md` for arrival of the TCL 55T6D.
- **Verification Gates**:
  - `git diff --check`: 0 errors.
  - `gradlew test`: 174 passed across 18 test suites (0 failures, 0 errors, 0 skipped).
  - `gradlew lint`: 0 errors, 62 warnings (matches original baseline).
  - `gradlew assembleDebug`: 1,955,886 bytes (+41,327 bytes vs baseline, +2,158 bytes vs M2 pre-hardening).

### Milestone 3: Multi-Row Home Container & Local Hero Showcase (STRICTLY BLOCKED)
- **Status**: **STRICTLY BLOCKED** — DO NOT BEGIN until physical hardware acceptance of Milestone 2 / 2.5 passes on the TCL 55T6D.
- **Scope**:
  - Multi-row vertical container supporting configurable rows:
    - Continue Watching (local items only)
    - My Watchlist (local persisted entries)
    - Trending Movies (local synthetic catalogue)
    - Popular Series (local synthetic catalogue)
    - Pinned Favorite Apps Dock
    - All Apps Library Grid
  - Hero area orchestrator driven by local synthetic data (Title, Year, Rating, Synopsis, Badges, Actions).
  - Reduced-motion mode and High-contrast focus state.
  - On-TV row visibility toggles in settings.
- **Verification Gates**:
  - Measure memory consumption under row scrolling on budget profile.
  - Verify zero network calls or image downloads.

### Milestone 4: Network & Caching Spike, Authorized Metadata Source (BLOCKED)
- **Status**: **BLOCKED** — Dependent on Milestone 3 completion.
- **Scope**:
  - Empirical memory class measurement via `ActivityManager.getMemoryClass()`.
  - Dynamic cache budget calculation (no static 16 MB assumptions).
  - Lightweight `HttpClient` spike using `HttpURLConnection`.
  - Integration of one authorized metadata source (TMDB, region IN, Tamil `ta-IN`).
  - Strict offline fallback: cached rendering and instant local app loading when network is unreachable.
  - Zero secrets committed (keys read from untracked `local.properties`).
- **Verification Gates**:
  - Offline startup test: launcher boots instantly with Wi-Fi disabled.
  - Schema validation and malformed JSON unit tests.

### Milestone 5: TV Probe, Safe Installation & Hardware Verification (PENDING HARDWARE)
- **Status**: **PENDING PHYSICAL DEVICE ARRIVAL**
- **Scope**:
  - Physical TCL 55T6D connection probe via `tools/tv.py --probe`.
  - Complete execution of `physical_tv_acceptance_checklist.md`.
  - Audit installed OTT packages (`pm list packages`).
  - Deploy release APK via `build-and-install.ps1 -StockLauncher none` (stock launcher preserved).
  - Physical TV verification of D-pad, screensaver, and provider launch fallbacks.
  - Full walkthrough report with TV screenshots.
