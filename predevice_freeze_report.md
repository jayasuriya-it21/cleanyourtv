# Pre-Device Freeze Audit Report

**Project:** CleanYourTV Content Hub Launcher
**Application ID:** `com.gathin.tvhome`
**Branch:** `feature/unified-content-launcher`
**Audit date:** 2026-10-22
**Status:** Freeze audit locally complete; checkpoint pending final commit

---

## 1. Final Verified Local Status

| Gate | Command | Result |
| :--- | :--- | :---: |
| Whitespace check | `git diff --check` | **PASS** (Exit code: 0; 0 whitespace errors; 3 LF to CRLF normalisation notices under Windows `core.autocrlf`) |
| Unit tests | `.\gradlew.bat test` | **PASS** (179 tests, 18 suites, 0 failures, 0 errors, 0 skipped) |
| Lint | `.\gradlew.bat lint` | **PASS** (0 errors, 62 warnings, 80 informational issues; exact baseline match) |
| Debug APK | `.\gradlew.bat assembleDebug` | **PASS** (1,956,522 bytes / 1,910.67 KB) |
| Untracked project documentation | `git ls-files --others --exclude-standard` | **1 file** (`predevice_freeze_report.md`) |
| Untracked sensitive / generated files | `git ls-files --others --exclude-standard` | **CLEAN** (none detected) |
| Working tree status | `git status --short` | **6 modified docs, 1 untracked doc** |

---

## 2. Unknown Device Attributes

The TCL 55T6D has been ordered but has **not physically arrived**. All of the following hardware and firmware properties remain unknown until a non-destructive hardware probe is executed on the physical unit:

| Attribute | Status |
| :--- | :---: |
| SoC / CPU model | **Unknown pending hardware probe** |
| RAM (total capacity) | **Unknown pending hardware probe** |
| Android / Google TV OS version | **Unknown pending hardware probe** |
| Firmware build fingerprint | **Unknown pending hardware probe** |
| Stock launcher package name | **Unknown pending hardware probe** |
| Display native resolution | **Unknown pending hardware probe** |
| Display refresh rate | **Unknown pending hardware probe** |

No assumptions or claims regarding MediaTek MT5895, ~1 GB RAM, Android 14, 4K resolution, 60 FPS, or `com.google.android.apps.tv.launcherx` are treated as verified facts. All repository documentation has been aligned to reflect these unknown attributes pending probe.

---

## 3. Configuration-Parser and Policy Architecture

### Current Implementation

In the production runtime path, configuration parsing is separated from policy resolution:

```java
// Config.java (Android runtime path) parses JSON and extracts a nullable Boolean:
Boolean overlaySpotlight = (json != null
        && json.opt(ConfigPolicy.KEY_SHOW_SYNTHETIC_SPOTLIGHT) instanceof Boolean)
    ? (Boolean) json.opt(ConfigPolicy.KEY_SHOW_SYNTHETIC_SPOTLIGHT)
    : null;

// Production policy receives a typed value, not raw JSON:
showSyntheticSpotlight = ConfigPolicy.resolveSpotlightEnabled(compiledDefault, overlaySpotlight);
```

### Architectural Division

- **Production Config parses JSON**: `Config.java` parses the external JSON payload using Android's JSON facilities.
- **Production ConfigPolicy receives a typed nullable Boolean**: `ConfigPolicy.resolveSpotlightEnabled(boolean compiledDefault, Boolean overlayValue)` contains pure policy logic without JSON parsing dependencies.
- **The raw-string overload is temporary and test-only**: `ConfigPolicy.resolveSpotlightEnabled(boolean compiledDefault, String rawJsonString)` operates via a minimal fallback regex parser to allow headless JVM test environments to run without `android.util.JSONObject`.
- **Separation of concerns**: Production policy is strictly separated from parsing, but the `ConfigPolicy` class still contains a temporary test helper that inspects raw strings.

### Temporary Regex Fallback Constraints

- Restricted strictly to a single root-level boolean key (`show_synthetic_spotlight`).
- Input length bounded indirectly by file-read limits (< 64 KB).
- Fails closed to `compiledDefault` on any malformed, ambiguous, or unparseable input.
- Rejects duplicate keys (count != 1 yields `null`).
- Rejects nested occurrences (brace depth at key position != 1 yields `null`).
- Rejects non-boolean tokens, quotes, numbers, and substrings.
- Verified by 32 dedicated unit tests in `ContentRowNavigationTest`.

### Future Cleanup Item

- Remove the test-only overload `ConfigPolicy.resolveSpotlightEnabled(boolean, String)` once headless JSON parsing can be tested independently (e.g., via Robolectric, a pure-Java parser abstraction, or mocking).

---

## 4. Documentation Corrections Applied Across Tracked Files

All modifications in this checkpoint are restricted to Markdown documentation files. No source code or resource files were changed during this freeze audit.

### `milestone_2_report.md`

| Item | Previous State | Corrected State |
| :--- | :--- | :--- |
| Header device metadata | `MediaTek MT5895 class, ~1 GB RAM profile` | Full unknown-pending-probe specification block |
| GlassView animation result | `smoothly springs back` | `expected…based on focus listener logic; pending integrated hardware verification` |
| Verification table panel | `55" 4K Display` | `55" Physical Panel; Display specifications unknown until hardware probe` |
| Verification environment | `Android Runtime Shell` | `Local Source Verification` |
| Physical tests item 3 | `MediaTek MT5895 / 1 GB RAM` | `physical SoC (unknown pending hardware probe)` |
| Physical tests item 4 | `4K resolution` | `display's native resolution` |

### `milestone_2_5_predevice_report.md`

| Item | Previous State | Corrected State |
| :--- | :--- | :--- |
| Header device metadata | `MediaTek MT5895 SoC profile, ~1 GB RAM profile` | Full unknown-pending-probe specification block |
| Disabled behavior label | `Local Logic/Source Verification` | `Local Source Verification` |
| Layout space claim | `Consumes exactly 0 layout space` | `Expected from View.GONE semantics: consumes zero layout space` |
| Navigation bindings | `maintain 100% of original` | `maintain original…(pending integrated hardware verification)` |
| Test count | `174` | `179` (current measured count) |
| Lint result summary | `completely resolved` | `resolved by Milestone 2.5 hardening` |
| Risk assessment (SoC/GPU) | `MediaTek MT5895 GPU Scaling; 4K resolution` | `Unknown SoC GPU Scaling; display's native resolution` |
| Risk assessment (RAM) | `RAM overhead must be verified` | `RAM budget is unknown pending hardware probe` |

### `physical_tv_acceptance_checklist.md`

| Item | Previous State | Corrected State |
| :--- | :--- | :--- |
| Header device metadata | `~1 GB RAM budget, MediaTek MT5895 class` | Full unknown-pending-probe specification block |
| ADB connection procedure | `adb connect <TV_IP>:5555` (hardcoded port) | Dual guidance: legacy network ADB (read port from UI) and wireless debugging pairing flow |
| Stock launcher probe | `e.g., com.google.android.apps.tv.launcherx` | `Record exact package name from HOME resolution. Do not assume a specific package before probing.` |
| Build command format | `launcher/gradlew.bat assembleDebug` (POSIX) | Fenced PowerShell commands with directory navigation |
| Config push procedure (Section E) | Inline shell command (`adb shell "echo ... > /path"`) | Multi-step PowerShell workflow using temporary file, backup, read-back verification, and restoration |
| Direct-write caveat | Not mentioned | `Direct writes to Android/data may be firmware-dependent; mark BLOCKED if refused` |
| GlassView verification | `completely hidden`, `smoothly fades in` | `expected from View.GONE semantics…pending integrated hardware verification` |
| Performance target gates | Fixed numeric thresholds (`< 600 ms`, `PSS < 60 MB`, `0 jank spikes > 50 ms`) | Measurement-first protocol: gather >= 5 samples, report medians with delta comparisons, define budgets from empirical data |
| Immediate failure criteria | Not explicitly defined | Crash, ANR, unrecoverable focus, and unintended HOME replacement classified as immediate test failures |

### `architecture.md`

| Item | Previous State | Corrected State |
| :--- | :--- | :--- |
| Principle 2 heading | `Strict Hardware Efficiency (1 GB RAM Budget)` | `Strict Hardware Efficiency (Memory Budget)` |
| Principle 2 note | Not present | Explicit clarification that target is provisionally low-memory Android TV; exact RAM unknown pending probe |
| Migration map claim | `zero regressions` | `no automated regression is introduced` |
| ContentIdentity snippet | Included `implements Serializable` and `serialVersionUID` | Removed (aligns with Milestone 2.5 code) |

### `implementation_plan.md`

| Item | Previous State | Corrected State |
| :--- | :--- | :--- |
| Executive summary | `zero regressions` | `no automated regression detected between milestones` |
| M2 physical verification item | `MediaTek MT5895 SoC` | `physical SoC (unknown pending hardware probe)` |

### `task.md`

| Item | Previous State | Corrected State |
| :--- | :--- | :--- |
| Hardware Profile entry | `Low-memory budget Android TV (~1 GB RAM)` | `Unknown pending hardware probe (provisionally targeting low-memory budget Android TV)` |

### `provider-capability-matrix.md`

- **Status**: No current diff. Already verified free of unverified device claims or machine-specific paths. Excluded from checkpoint staging proposal.

---

## 5. Repository Hygiene Findings

### Exact `git status --short`

```text
 M architecture.md
 M implementation_plan.md
 M milestone_2_5_predevice_report.md
 M milestone_2_report.md
 M physical_tv_acceptance_checklist.md
 M task.md
?? predevice_freeze_report.md
```

### Exact `git diff --name-only`

```text
architecture.md
implementation_plan.md
milestone_2_5_predevice_report.md
milestone_2_report.md
physical_tv_acceptance_checklist.md
task.md
```

### Exact `git ls-files --others --exclude-standard`

```text
predevice_freeze_report.md
```

### Exact `git diff --check`

```text
Exit code: 0
Whitespace errors: 0
Notice: Output contains 3 LF to CRLF normalisation warnings for architecture.md, implementation_plan.md, and task.md under Windows core.autocrlf configuration.
```

### Untracked Files Status

- **Untracked project documentation**: `predevice_freeze_report.md` (new freeze audit report, ready for proposed staging).
- **Untracked sensitive / generated files**: None detected.

### Sensitive Pattern and Path Search Results

The 6 modified documentation files and the untracked `predevice_freeze_report.md` were scanned for sensitive strings and machine-specific paths:

| Search Pattern | Scope | Result | Details |
| :--- | :--- | :---: | :--- |
| User profile path (`C:\Users\`) | Changed & untracked docs | **CLEAN** | Zero matches across audit-scope documents |
| Workspace root path (`d:\Documents\`) | Changed & untracked docs | **CLEAN** | Zero matches across audit-scope documents |
| URI scheme (`file:///`) | Changed & untracked docs | **CLEAN** | Only documentary reference in `milestone_2_5_predevice_report.md` noting removal of `file:///` URLs |
| Private network (`192.168.`) | Changed & untracked docs | **CLEAN** | Zero actual IP addresses in audit-scope files (historical documentation examples exist in pre-existing tracked docs) |
| Private key header (`BEGIN PRIVATE KEY`) | Changed & untracked docs | **CLEAN** | Zero matches |
| Credentials (`api_key`, `access_token`, `password`, `secret`) | Changed & untracked docs | **CLEAN** | Zero actual secrets; only benign documentary references explaining security policies |

### Files Confirmed Excluded and Untracked

| Path / Pattern | Tracking Status | Rationale |
| :--- | :---: | :--- |
| `launcher/local.properties` | Untracked | Local Android SDK path; gitignored |
| `launcher/.gradle/` | Untracked | Gradle daemon cache; gitignored |
| `launcher/app/build/` | Untracked | Compilation and test artifacts; gitignored |
| `*.apk`, `*.aab` | Untracked | Binary application packages; gitignored |
| `tools/adbkey/` | Untracked | Local ADB keys; gitignored |
| `backup/` | Untracked | Device backup dumps; gitignored |
| `.freebuff/` | Untracked | IDE workspace metadata; gitignored |
| Antigravity scratch directory outside the repository | Untracked | Outside workspace; never committed |

---

## 6. Test Results

- **Command**: `.\gradlew.bat test` (executed from `launcher/`)
- **Report Source**: `launcher/app/build/test-results/testDebugUnitTest/*.xml`

| Metric | Measured Value |
| :--- | :---: |
| **Total test methods** | **179** |
| **Test suites (classes)** | **18** |
| **Failures** | **0** |
| **Errors** | **0** |
| **Skipped** | **0** |

### Milestone Progression

| Milestone Stage | Test Methods | Net Delta |
| :--- | :---: | :---: |
| Original baseline | 120 | — |
| Milestone 1 | 142 | +22 |
| Milestone 2 | 155 | +13 |
| Milestone 2.5 | 174 | +19 |
| **Freeze audit (current)** | **179** | **+5** |

---

## 7. Lint Results

- **Command**: `.\gradlew.bat lint` (executed from `launcher/`)
- **Report Source**: `launcher/app/build/reports/lint-results-debug.xml`

| Metric | Measured Value | Baseline Comparison |
| :--- | :---: | :---: |
| **Lint errors** | **0** | Exact baseline match (0) |
| **Lint warnings** | **62** | Exact baseline match (62) |
| **Informational issues** | **80** | Baseline match (80) |
| **Total issues reported** | **142** | Exact baseline match (142) |

---

## 8. APK Metrics and Language

- **Command**: `.\gradlew.bat assembleDebug` (executed from `launcher/`)
- **Artifact**: `launcher/app/build/outputs/apk/debug/app-debug.apk`
- **Measured Size**: **1,956,522 bytes** (1,910.67 KB)

| Build Stage | APK Size (Bytes) | Size (KB) | Delta vs Baseline | Delta vs Prior Build |
| :--- | :---: | :---: | :---: | :---: |
| Original baseline | 1,914,559 | 1,869.69 KB | 0 | — |
| Milestone 1 | 1,928,655 | 1,883.45 KB | +14,096 B (+0.74%) | +14,096 B |
| Milestone 2 | 1,953,728 | 1,907.94 KB | +39,169 B (+2.05%) | +25,073 B |
| Milestone 2.5 | 1,955,886 | 1,910.05 KB | +41,327 B (+2.16%) | +2,158 B |
| **Freeze audit (current)** | **1,956,522** | **1,910.67 KB** | **+41,963 B (+2.19%)** | **+636 B** |

### APK Attribution Statement

The current APK differs by 636 bytes from the previously recorded build. No runtime source changes were made during the freeze audit. The difference has not been independently attributed.

APK size represents the disk footprint of the packaged application package and resources; it does not measure or imply runtime RAM consumption.

---

## 9. Proposed Staged Files (Documentation Checkpoint Only)

The working tree contains exactly six modified Markdown files and one untracked Markdown file. All launcher source code, resources, and tests were committed in earlier milestone commits on `feature/unified-content-launcher` and remain unchanged relative to `HEAD`. `provider-capability-matrix.md` has no diff in this audit and is not included.

The proposed checkpoint scope consists strictly of the following seven documentation files:

1. `architecture.md`
2. `implementation_plan.md`
3. `milestone_2_report.md`
4. `milestone_2_5_predevice_report.md`
5. `physical_tv_acceptance_checklist.md`
6. `task.md`
7. `predevice_freeze_report.md`

### Proposed Staging Command (DO NOT EXECUTE YET)

```powershell
git add -- architecture.md implementation_plan.md milestone_2_report.md milestone_2_5_predevice_report.md physical_tv_acceptance_checklist.md task.md predevice_freeze_report.md
```

> [!IMPORTANT]
> This command is proposed for review only. It has NOT been executed. No files have been staged, committed, or pushed.

---

## 10. Explicitly Excluded Files (Must Remain Unstaged)

| File / Directory | Rationale |
| :--- | :--- |
| `launcher/local.properties` | Local machine Android SDK directory; gitignored |
| `launcher/.gradle/` | Local Gradle cache directory; gitignored |
| `launcher/app/build/` | Build outputs, APKs, test reports; gitignored |
| `tools/adbkey/` | Private ADB cryptographic keys; gitignored |
| `backup/` | Local hardware backup dumps; gitignored |
| `.freebuff/` | Local editor/IDE cache; gitignored |
| `*.apk`, `*.aab` | Compiled binaries; gitignored |
| Antigravity scratch directory outside the repository | External runtime scratch space; outside workspace |
| Committed source, resource, or test files | Unchanged in `HEAD`; already committed in prior milestones |

---

## 11. Remaining Physical Tests (Blocked)

All items below remain **BLOCKED** until the ordered TCL 55T6D physically arrives and Section A (stock baseline) and Section B (ADB connection) of `physical_tv_acceptance_checklist.md` are completed:

1. Non-destructive hardware probe: SoC, RAM, Android version, firmware fingerprint, stock launcher package name, native display resolution, refresh rate.
2. D-pad remote navigation responsiveness, key debounce handling, and tactile latency.
3. Bluetooth/IR remote repeat-key handling under rapid directional input.
4. Cold-start and warm-start launch latency measurement (>= 5 samples, Spotlight ON vs. OFF).
5. Runtime PSS memory consumption profiling via `dumpsys meminfo`.
6. Frame-rendering metrics via `dumpsys gfxinfo` under 2-minute sustained navigation stress test.
7. Visual rendering of the 6 vector poster drawables on the physical panel.
8. GlassView focus-spring animation and fade-in/fade-out aesthetic validation.
9. Verification of external config push to `Android/data` (firmware permission dependent).
10. Execution and sign-off of all sections (A through G) of `physical_tv_acceptance_checklist.md`.

---

## 12. NO-GO for Milestone 3

> [!CAUTION]
> **MILESTONE 3 IS STRICTLY BLOCKED — EXPLICIT NO-GO**
>
> The `feature/unified-content-launcher` branch is frozen at Milestone 2.5 pre-device hardening.
>
> Milestone 3 (Multi-Row Home Container & Local Hero Showcase) **MUST NOT BEGIN** until:
>
> 1. The ordered TCL 55T6D physically arrives.
> 2. The non-destructive hardware probe (Section C of `physical_tv_acceptance_checklist.md`) is completed and all actual device specifications are recorded.
> 3. All sections A through G of `physical_tv_acceptance_checklist.md` pass on physical hardware.
> 4. A verified physical acceptance report is committed to the repository.
>
> The following operations are strictly prohibited prior to physical acceptance:
> - Installing any APK on the TV.
> - Attempting any ADB connection.
> - Changing or requesting the `android.intent.category.HOME` default launcher.
> - Modifying package enabled/disabled states.
> - Executing debloat or restore scripts.
> - Modifying screensaver or display settings.

---

## 13. Recommended Checkpoint Commit Message

```text
docs: pre-device freeze audit — correct unverified device claims and harden documentation

Align all project documentation with the pre-device freeze policy. Replace
unverified assertions about SoC (MT5895), RAM (~1 GB), Android version, firmware,
stock launcher package, and display mode with "unknown pending hardware probe".

Scope: Documentation only (7 files)
- architecture.md: update memory budget principle; remove Serializable from snippet
- implementation_plan.md: align regression wording; update SoC reference
- milestone_2_report.md: align device header, verification levels, and display references
- milestone_2_5_predevice_report.md: update header, View.GONE semantics, test count (179)
- physical_tv_acceptance_checklist.md: add dual ADB guidance, PowerShell config push,
  measurement-first performance policy, failure criteria, and generic temp paths
- task.md: replace RAM assumption with unknown pending probe
- predevice_freeze_report.md: add comprehensive pre-device freeze audit report

Quality gates (freeze audit):
  git diff --check: exit code 0 (0 errors, benign Windows CRLF notices)
  gradlew test: 179 tests, 18 suites, 0 failures, 0 errors, 0 skipped
  gradlew lint: 0 errors, 62 warnings, 80 informational (exact baseline match)
  gradlew assembleDebug: 1,956,522 bytes (+41,963 B vs baseline; +636 B vs M2.5)

Milestone 3 remains STRICTLY BLOCKED pending physical TCL 55T6D arrival and acceptance.
```

---

*Freeze audit locally complete. Checkpoint pending final commit. Milestone 3 remains NO-GO.*
