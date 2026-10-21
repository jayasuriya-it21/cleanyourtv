# Project Rules: CleanYourTV Content Hub Launcher

These rules govern all architectural decisions, code changes, and hardware operations for the CleanYourTV project and the `com.gathin.tvhome` application.

## 1. Safety and Device Preservation
- **Preserve Recovery Scripts**: Never modify `tools/disable.sh`, `tools/disable.ps1`, `tools/restore.sh`, `tools/restore.ps1`, or existing recovery behavior unless fixing a proven, documented bug.
- **No Debloating During Launcher Development**: Do not run debloat scripts or disable packages as part of developing, building, or testing launcher features.
- **Stock Launcher Preservation**: During development and installation, the stock launcher must remain enabled as a recovery fallback. Always pass `-StockLauncher none` (or equivalent) until the full acceptance test suite passes.
- **Explicit Launch Validation**: Before changing the default `android.intent.category.HOME` activity, verify that the customized launcher starts cleanly via an explicit activity launch (`am start -W -n com.gathin.tvhome/.MainActivity`).
- **Immediate Recovery on Failure**: If default HOME switching or launcher startup fails, immediately preserve or re-enable the stock launcher.
- **No Destructive Operations**: Never run commands that clear user data, wipe partitions, or execute unverified root exploits.

## 2. Security and Privacy
- **Zero Secrets in Source Control**: Never commit API keys, server credentials, access tokens, PINs, signing keystores, TV IP addresses, or personal configuration to Git. Use untracked configuration (`local.properties` or runtime files).
- **No User Credential Handling**: Never request, collect, store, or transmit third-party OTT usernames, passwords, session tokens, or cookies.
- **No Private Data Scraping**: Never scrape an OTT application's private UI, accessibility node hierarchy, sandbox files, databases, network traffic, or DRM-protected streams.
- **Honest Security Boundaries**: A DreamService PIN or Child/Guest gate is a soft launcher filter, never an Android security boundary or cryptographic lock screen. Clearly state this in UI and documentation.
- **Network Security**: Never globally enable `android:usesCleartextTraffic="true"`. Restrict cleartext communication strictly to user-configured local network endpoints via `<network-security-config>`.
- **Zero Telemetry**: No background telemetry, analytics pings, advertising SDKs, or query tracking may be added to the launcher.

## 3. Technology Stack and Platform Constraints
- **Preserve Java Baseline**: Maintain the repository's clean, lightweight Java 17 implementation. Do not convert existing classes to Kotlin or introduce Jetpack Compose unless there is a proven, documented necessity that respects the 1 GB RAM budget.
- **Minimal Dependencies**: Rely on AndroidX core libraries (`androidx.recyclerview`, `androidx.dynamicanimation`) and standard Android APIs. Evaluate any new dependency for APK size impact and memory overhead before adoption.
- **Target Hardware Reality**: The launcher must perform smoothly on low-memory Android TV hardware (1 GB RAM, budget quad-core SoCs, e.g. MediaTek MT5895/Realtek).
  - No continuous background services.
  - No real-time Gaussian blurs or expensive view shaders.
  - No autoplay video previews in rows or hero banners.
  - Strict bounded memory and disk caching for bitmaps.

## 4. TV Interaction and D-Pad UX
- **Remote-First Design**: Every visible interactive element must be navigable using standard D-pad Up/Down/Left/Right/Center(OK)/Back keys.
- **Unbreakable Focus**: Focus must never disappear, get stuck in a recursive loop, or land on off-screen invisible views.
- **Focus Restoration**: Returning to Home from an opened application, dialog, or settings must reliably restore focus to the previously active card or row.
- **Predictable Back Key**: The Back button must dismiss menus, dialogs, search inputs, or player overlays before prompting or exiting to Home.
- **No Focus Trapping in Nested Lists**: Nested horizontal RecyclerView rows inside vertical lists must handle vertical transitions deterministically.

## 5. Network and Offline Resilience
- **Offline-First Home**: The launcher must boot, render all installed apps, and remain fully navigable with zero network connectivity or if external APIs return HTTP errors.
- **Network Isolation**: All network operations must run on background thread pools with connection/read timeouts, exponential backoff, response size limits, and schema validation.
- **Graceful Degradation**: When network catalogue rows or metadata fail, display cached content or cleanly hide the empty shelf without interrupting launcher shell operation.
- **Stale-While-Revalidate**: Render cached rows immediately upon launch, updating seamlessly in the background without jumping focus.

## 6. Provider Integrations and Deep Links
- **Strict Honesty in Capabilities**: A provider integration must advertise only its verified, device-tested capability (`OPEN_APP`, `OPEN_SEARCH`, `OPEN_TITLE`, `OPEN_PLAYBACK`).
- **Never Fabricate Deep Links**: Never construct speculative URI schemes. If a deep link is unverified, fall back safely to in-app search or opening the provider application.
- **Clear Fallback Hierarchy**:
  1. Verified title deep link
  2. Verified provider search intent
  3. Android TV global search intent
  4. Explicit package launch
  5. Clear "Unsupported on this device" dialog
- **Subscription Honesty**: Catalogues and metadata indicate title existence, never guaranteed subscription availability for the current user.

## 7. Testing, Verification, and Performance
- **Automated Test Coverage**: All data parsers, normalizers, cache policies, configuration loaders, and intent builders must have JUnit unit tests.
- **Empirical Performance Measurement**: Do not claim performance targets without measurement. Measure APK size, cold start, memory footprint, and dropped frames under real or profiled conditions.
- **Physical Device Gate**: Features touching TV hardware (remote buttons, HDMI inputs, screensaver, provider deep links) must be tested on real hardware or cleanly marked as untestable in the current environment.
