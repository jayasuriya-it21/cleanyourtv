# CleanYourTV Content Hub: Master Task Tracker

## Target Specifications
- **Device**: TCL 55T6D (Google TV, India region)
- **Primary Languages**: Tamil (ta), English (en)
- **Target OTT Apps**: Netflix, Amazon Prime Video, Disney+ Hotstar / JioHotstar, SunNXT, Zee5, SonyLIV, Aha Tamil, YouTube, VLC, Kodi, Jellyfin
- **SoC / RAM / OS**: Unknown pending hardware probe (provisionally targeting low-memory budget Android TV)
- **Branch**: `feature/unified-content-launcher`

---

## Milestone Checklist

### Phase 0: Repository Audit & Baseline Verification
- [x] Complete source and manifest audit (`MainActivity`, `AppAdapter`, `ClockDream`, `Config`, `Order`, `LibraryApps`, `Cards`)
- [x] Baseline build: `assembleDebug` successful (APK size: 1,914,559 bytes ~ 1.83 MB)
- [x] Baseline test: `testDebugUnitTest` successful (120 tests passed, 0 failures, 0 errors)
- [x] Baseline lint: `lintDebug` successful (0 errors, 62 warnings)
- [x] Target TV probe: Confirmed disconnected, ready for connection
- [x] Git branch created: `feature/unified-content-launcher`

### Phase 1: Planning Input Corrections
- [x] Correct provider capability states (`provider-capability-matrix.md`) with explicit states (`NOT_IMPLEMENTED`, `IMPLEMENTED_UNTESTED`, `RESOLVES_ON_DEVICE`, `VERIFIED_ON_DEVICE`)
- [x] Correct playback progress claims: No false progress for OTT or external video players; labeled "Recently Opened"
- [x] Correct content ID disambiguation: Disjoint metadata ID and provider content IDs
- [x] Correct cache assumptions: Heap-relative dynamic budgeting via `ActivityManager.getMemoryClass()`
- [x] Clean architecture documentation: Fenced code blocks, migration map, existing vs. planned labels

### Milestone 1: Domain Foundation & App Discovery Extraction (Completed)
- [x] Implement `com.gathin.tvhome.apps.InstalledApp`
- [x] Implement `com.gathin.tvhome.apps.AppCategory`
- [x] Implement `com.gathin.tvhome.content.model.ContentIdentity`
- [x] Implement `com.gathin.tvhome.content.model.ContentItem` (with separated metadata & provider ID concepts)
- [x] Implement `com.gathin.tvhome.content.model.ContentRow`
- [x] Extract `com.gathin.tvhome.apps.AppDiscoveryManager`
  - [x] Leanback launcher intent query
  - [x] Launcher intent query for sideloaded apps
  - [x] Prefer Leanback activities when package exposes both
  - [x] Deduplicate components and packages
  - [x] Exclude self (`com.gathin.tvhome`)
  - [x] Exclude unexported or disabled activities
  - [x] Preserve `hidden_apps` filtering
  - [x] Preserve `dock_apps` ordering
  - [x] Preserve user ordering (`Order.java`)
  - [x] Preserve App Library selections (`LibraryApps.java`)
  - [x] Mark non-Leanback apps as requiring touch / phone-only
- [x] Integrate compatibility bridge in `AppAdapter.java` (preserving 100% visual & interaction behavior)
- [x] Unit tests for `AppDiscoveryManager` pure normalization logic (18 tests)
- [x] Validation: `gradlew test` (142 passed, 17 suites, 0 failures, 0 errors, 0 skipped), `gradlew lint` (0 errors, 62 warnings), `gradlew assembleDebug` (1,928,655 bytes)
- [x] APK size and regression comparison (+14,096 bytes / +0.74%)

### Milestone 2: Synthetic Content Row & D-Pad Navigation Engine
- [x] **Automated Implementation Complete**:
  - [x] Single synthetic horizontal `ContentRow` ("Tamil Spotlight" with 6 original placeholder items)
  - [x] Zero network, zero external assets: bundled Android XML vector drawables only
  - [x] Integration with existing Top Bar, Dock, and App Grid
  - [x] Deterministic D-pad navigation engine (`RowNavigationManager`)
  - [x] Focus restoration engine remembering last-focused card/tile upon activity return (`FocusMemory`)
  - [x] GlassView compatibility: dedicated card focus treatment avoiding collision
  - [x] Unit tests passing (232 tests across 26 suites)
- [x] **Physical Acceptance Completed (2026-10-24)**:
  - [x] Hardware verification completed on physical TCL 55T6D (Realtek RTD6748, Android 14 SDK 34)
  - [x] Physical remote responsiveness, D-pad matrix, and tactile latency verified across all 11 test scenarios
  - [x] Runtime memory (PSS) measured under physical profiling (106 MB baseline, 116 MB with Spotlight; delta +9.7 MB)
  - [x] Verified full rollback and stock launcher integrity (ORIGINAL_HOME_ACTIVITY unchanged)

### Milestone 2.5: Pre-Device Hardening & Prototype Gating (Completed)
- [x] Correct Milestone 2 report: status updated to `AUTOMATED QUALITY GATES PASSED; PHYSICAL ACCEPTANCE COMPLETED`
- [x] Comprehensive lint audit: 0 errors
- [x] Flattened `item_content_poster.xml` root view into `LinearLayout`
- [x] Disabled prototype by default: `show_synthetic_spotlight` set to `false` in `config.xml`; pure Java `ConfigPolicy` for runtime JSON overlay resolution
- [x] Top utility candidate audit: candidate ranking logic in `RowNavigationManager`, strict runtime validation, safe absorption
- [x] Focus restoration hardening: lifecycle safety gates, user focus movement checks, feature-disabled clearing
- [x] DPAD event consumption audit: boundary absorption, key ownership, repeat event handling
- [x] GlassView safety audit: verified ID isolation, global focus listener dismissal, and destruction callback cleanup
- [x] Domain model safety check: pure immutable records
- [x] Created `physical_tv_acceptance_checklist.md` (Sections A through G completed)
- [x] Unit tests: 232 passing across 26 suites (0 failures, 0 errors, 0 skipped)
- [x] Physical-TV acceptance test passed with verified GO

### Milestone 3A: Local Multi-Row Shell (NEXT SAFE MILESTONE - UNBLOCKED)
- [ ] Two local synthetic rows only (Tamil Spotlight + Second Shelf)
- [ ] Original placeholder content & local drawables only
- [ ] Local details overlay
- [ ] Zero network calls, zero provider adapters, zero playback, zero database
- [ ] Physical D-pad retest on TCL 55T6D

### Future Gated Milestones (Sequentially Gated)
- [ ] Milestone 3B: Network and bounded cache design spike (bounded memory/disk cache, cached-first rendering)
- [ ] Milestone 3C: Universal search and launcher-owned watchlist
- [ ] Milestone 3D: Provider capability adapters (strict matrix validation, zero credential scraping)
- [ ] Milestone 3E: Local media integration
- [ ] Milestone 3F: Screensaver enhancement (ClockDream)
- [ ] Milestone 3G: Optional launcher takeover (final phase, never debloating)
