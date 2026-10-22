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
  - [x] Unit tests passing (155 tests in baseline M2)
- [ ] **Physical Acceptance Pending**:
  - [ ] Hardware verification blocked pending arrival of ordered TCL 55T6D
  - [ ] Physical remote responsiveness, key bounce, and tactile latency unverified
  - [ ] Runtime memory (PSS) and frame rate under hardware profiling unmeasured

### Milestone 2.5: Pre-Device Hardening & Prototype Gating (Completed Locally)
- [x] Correct Milestone 2 report: status updated to `AUTOMATED QUALITY GATES PASSED; PHYSICAL ACCEPTANCE PENDING`, unsupported claims removed, formatting cleaned
- [x] Comprehensive lint audit: eradicated all 8 newly introduced warnings, returning exact baseline of 62 warnings, 0 errors
- [x] Flattened `item_content_poster.xml` root view into `LinearLayout` (eliminates `UselessParent`)
- [x] Disabled prototype by default: `show_synthetic_spotlight` set to `false` in `config.xml`; pure Java `ConfigPolicy` for runtime JSON overlay resolution
- [x] Top utility candidate audit: candidate ranking logic in `RowNavigationManager`, strict runtime validation (attached, visible, enabled, focusable, non-zero size), safe absorption when no target valid
- [x] Focus restoration hardening: lifecycle safety gates, user focus movement checks, feature-disabled clearing
- [x] DPAD event consumption audit: boundary absorption, key ownership, repeat event handling
- [x] GlassView safety audit: verified ID isolation, global focus listener dismissal, and destruction callback cleanup
- [x] Domain model safety check: removed `Serializable`, confirmed pure immutable records
- [x] Created `physical_tv_acceptance_checklist.md` (Sections A through G)
- [x] Unit tests: 174 passing across 18 suites (0 failures, 0 errors, 0 skipped)
- [ ] Physical-TV portion remains **PENDING** until TCL 55T6D arrives

### Milestone 3: Multi-Row Home Container & Local Hero Showcase (BLOCKED)
- [ ] **BLOCKED**: Requires physical acceptance of Milestone 2 / 2.5 on TCL 55T6D hardware prior to commencement
- [ ] Multi-row vertical container (Continue Watching, Watchlist, Trending, Series, Dock, All Apps)
- [ ] Local Hero Showcase controller
- [ ] Reduced motion & high contrast focus
- [ ] Configurable row visibility

### Milestone 4: Network & Caching Spike, Authorized Metadata Source (BLOCKED)
- [ ] Dynamic cache budgeting via `ActivityManager`
- [ ] Lightweight `HttpClient` spike
- [ ] TMDB metadata integration (India region, Tamil priority)
- [ ] Offline resilience & cached rendering

### Milestone 5: Physical TV Probe, Safe Installation & Acceptance Testing (PENDING HARDWARE)
- [ ] Connect and probe TCL 55T6D via `tools/tv.py --probe`
- [ ] Deploy with `-StockLauncher none`
- [ ] Verify 24-point acceptance test suite
- [ ] Complete `physical_tv_acceptance_checklist.md`
