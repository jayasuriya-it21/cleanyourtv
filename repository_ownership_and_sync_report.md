# Repository Ownership and Upstream Synchronization Report

**Project:** CleanYourTV Content Hub Launcher
**Application ID:** `com.gathin.tvhome`
**Date:** 2026-10-22
**Status:** CHECKPOINT COMMITTED â€” UPSTREAM INTEGRATION COMPLETED LOCALLY â€” READY FOR ORIGIN AUTHENTICATION & PUSH

---

## 1. Repository and Remote Verification

| Remote Name | Remote URL | Push Policy | Verification Status |
| :--- | :--- | :--- | :---: |
| **`origin`** | `https://github.com/jayasuriya-it21/cleanyourtv.git` | Push authorized to personal repository only | **CONFIRMED PERSONAL REPOSITORY** |
| **`upstream`** | `https://github.com/Gathin23/cleanyourtv.git` | `DISABLED` (fetch only) | **CONFIRMED ORIGINAL UPSTREAM** |

- **Remote Protection:** Accidental pushes to upstream are permanently blocked via `git remote set-url --push upstream DISABLED`.
- **Zero Secrets Committed:** No API keys, credentials, tokens, SSH keys, private paths, or TV IP addresses are tracked.

---

## 2. Commit and Branch Hashes

| Reference | Branch / Commit Role | Full Commit Hash | Commit Summary |
| :--- | :--- | :--- | :--- |
| **Pre-Device Checkpoint** | Documentation freeze checkpoint | `3580ac09576cde720caa7e4668fb3170f4e6e79a` | `docs: finalize pre-device freeze and hardware acceptance plan` |
| **Recovery Snapshot** | `backup/pre-upstream-sync-20261022` | `3580ac09576cde720caa7e4668fb3170f4e6e79a` | Pointing to pre-device freeze checkpoint |
| **Upstream Default Branch** | `upstream/main` | `4c55533a47012eb9520199b801e8f1245b21cff3` | `README: counts and card after Round O's second batch` |
| **Local Base Branch** | `main` | `4c55533a47012eb9520199b801e8f1245b21cff3` | Clean fast-forward to `upstream/main` |
| **Integration Branch** | `integration/upstream-20261022` | `5b47980cdb90a61250ae4df9eef11082ce70e2be` | `merge: integrate upstream CleanYourTV updates through 4c55533a` |
| **Customization Branch (Current)** | `feature/unified-content-launcher` | `1f88c703d7c3ee1918335dbce2a60b0fc6d7554e` | `merge: integrate upstream CleanYourTV updates into feature/unified-content-launcher` |
| **Merge Base** | Common historical base | `66b7214a1d876279db6cb61aa5c3ccfd2d8b5eb2` | `README: Product Hunt badge says live now` |

---

## 3. Divergence and Upstream Change Summary

- **Common Merge Base:** `66b7214a`
- **Upstream-Only Commits:** 615 commits (Releases through `v2.6.0`, 30+ TV model debloat profiles, async app library loading, clock screensaver, Scenes hourly wallpapers, and new benchmarking tools).
- **Custom-Only Commits Prior to Merge:** 2 commits (`8b0862b` M2.5 code + `3580ac0` pre-device freeze documentation).

---

## 4. Conflict Resolution Summary

During the integration merge, five files had merge conflicts. Every conflict was resolved intentionally preserving customization features and upstream bug fixes:

1. **`AGENTS.md`**: Combined both sets of guidelines: our repository project rules (safety, Java 17 baseline, remote-first D-pad UX, and physical TV gates) at the top, followed by upstream contributing instructions below.
2. **`Config.java`**: Retained custom `showSyntheticSpotlight` boolean and `ConfigPolicy.resolveSpotlightEnabled` delegation alongside all 20+ upstream configuration properties.
3. **`MainActivity.java`**: Retained `btnSettings` and all D-pad spotlight navigation methods (`handleSpotlightKey`, `handleSpotlightUp`, `focusSpotlightPoster`, etc.) while integrating upstream `wifiIcon`/`pillsShown` caching and `firstTopButton()` navigation.
4. **`AppAdapter.java`**: Reconciled `reload` signature `reload(cards, leanback)` while continuing to use `AppDiscoveryManager.discoverHomeApps(...)`.
5. **`AppLibraryDialog.java`**: Adopted upstream's asynchronous `LOADER.execute(...)` off-thread loader and `LibraryLoads` caching.

---

## 5. Final Quality Gate Results

All quality gates passed on the merged customization branch `feature/unified-content-launcher`:

| Quality Gate | Measured Result | Status |
| :--- | :---: | :---: |
| **Whitespace & Formatting (`git diff --check`)** | Exit code 0, 0 whitespace errors | **PASS** |
| **Unit Test Suites** | **26 suites** (18 baseline + 8 upstream suites) | **PASS** |
| **Total Test Methods** | **232 tests** (179 baseline + 53 upstream tests) | **PASS** |
| **Test Failures / Errors / Skipped** | **0 failures, 0 errors, 0 skipped** | **PASS** |
| **Lint Report (`.\gradlew.bat lint`)** | **0 errors**, 75 warnings, 97 informational | **PASS** |
| **Debug APK (`.\gradlew.bat assembleDebug`)** | **2,037,157 bytes** (1,989.41 KB) | **PASS** |

---

## 6. Origin Push and Security Status

- **Upstream Push Status:** Permanently disabled (`DISABLED`). Confirmed **NOTHING** was pushed or will ever be pushed to upstream.
- **Force Push Status:** Zero force pushes used (`--force` and `--force-with-lease` strictly forbidden).
- **Origin Push Status:** Dry-run push to `origin` (`git push --dry-run origin main` / `git push --dry-run origin feature/unified-content-launcher`) returned `remote: Repository not found`.
  - **Security Gate Triggered:** Per project policy, no credentials or personal access tokens may be requested in chat or placed in command lines.
  - **Required User Action:** The user must ensure the repository `https://github.com/jayasuriya-it21/cleanyourtv` exists on GitHub and authenticate locally using GitHub CLI (`gh auth login`), Windows Credential Manager, or an SSH remote URL before executing the final push.

---

## 7. Instructions for Future Upstream Synchronization

Refer to the newly created documentation guides:
- **`UPSTREAM_SYNC.md`**: Complete step-by-step procedure and permanent branching architecture.
- **`UPSTREAM_SYNC_CHECKLIST.md`**: Pre-merge and post-merge verification checklist.

Summary workflow for future syncs:
1. Verify working tree is clean (`git status --short`).
2. Fetch upstream (`git fetch upstream --prune --tags`).
3. Fast-forward local `main` (`git switch main; git merge --ff-only upstream/main; git switch feature/unified-content-launcher`).
4. Create backup branch (`backup/pre-upstream-sync-YYYYMMDD`).
5. Create temporary integration branch (`integration/upstream-YYYYMMDD`).
6. Perform non-final merge (`git merge --no-commit --no-ff upstream/main`).
7. Resolve conflicts, run tests and lint.
8. Commit integration branch and merge back into `feature/unified-content-launcher` with `--no-ff`.
9. Push strictly to `origin`.

---

## 8. Milestone 3 & Physical TV Acceptance Gate Status

> [!CAUTION]
> **MILESTONE 3 REMAINS STRICTLY BLOCKED â€” EXPLICIT NO-GO**
>
> 1. The TCL 55T6D has been ordered and is awaiting physical arrival.
> 2. No ADB connections, APK installations, debloating, HOME replacements, or device interactions will be attempted.
> 3. Features touching TV hardware must be verified on physical hardware per `physical_tv_acceptance_checklist.md` before Milestone 3 development begins.
