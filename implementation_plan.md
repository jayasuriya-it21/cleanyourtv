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

### Milestone 2: Synthetic Content Row & D-Pad Navigation Engine (Completed)
- **Automated Implementation**: Single horizontal synthetic `ContentRow`, D-pad navigation (`RowNavigationManager`), focus restoration (`FocusMemory`), GlassView isolation, 232 unit tests passing.
- **Physical Verification**: Fully completed on TCL 55T6D (Realtek RTD6748, Android 14 SDK 34) on 2026-10-24. 11/11 D-pad navigation scenarios passed. Verified complete rollback.

### Milestone 2.5: Pre-Device Hardening & Prototype Gating (Completed)
- Verified default-off prototype state (`show_synthetic_spotlight = false`), runtime config override, top utility candidate ranking, focus restoration guards, and zero package modifications.

### Milestone 3A: Local Multi-Row Shell (NEXT SAFE MILESTONE - UNBLOCKED)
- **Status**: **UNBLOCKED â€” READY FOR IMPLEMENTATION**
- **Scope**:
  - Two local synthetic rows only (Tamil Spotlight + Second Shelf).
  - Original placeholder content & bundled vector drawables only.
  - Local details overlay.
  - Zero network calls, zero provider adapters, zero playback, zero database.
  - Physical D-pad retest on TCL 55T6D.

### Subsequent Sequentially Gated Milestones
- **Milestone 3B**: Network and cache design spike (bounded memory and disk cache, cached-first rendering, offline resilience).
- **Milestone 3C**: Universal search and launcher-owned watchlist.
- **Milestone 3D**: Provider capability adapters (strict capability matrix, zero scraping, verified Intent targets).
- **Milestone 3E**: Local media integration.
- **Milestone 3F**: Screensaver enhancement (ClockDream).
- **Milestone 3G**: Optional launcher takeover (only as final step, never debloating).
