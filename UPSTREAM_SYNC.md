# CleanYourTV Upstream Synchronization Workflow

This document establishes the official, safe, long-term workflow for synchronizing this customized CleanYourTV repository with the upstream original repository (`https://github.com/Gathin23/cleanyourtv.git`) while protecting all custom TV launcher enhancements in your own GitHub repository (`origin`).

---

## 1. Permanent Branching Model

| Branch | Role | Protection Rules |
| :--- | :--- | :--- |
| `upstream/main` | **Read-Only Upstream Tracker** | Tracks the original repository HEAD. Never modified directly. Never pushed to. |
| `main` | **Synchronized Base Branch** | Represents the upstream base branch cleanly mirrored into your own GitHub repository. Fast-forwarded only. No direct TV feature work. |
| `feature/unified-content-launcher` | **Primary Customization Branch** | Contains all custom TV Content Hub launcher architecture, Milestones 1, 2, and 2.5 implementations, and documentation. Receives upstream changes only after isolated validation. |
| `integration/upstream-YYYYMMDD` | **Temporary Integration Branch** | Created on demand for each upstream sync cycle from `feature/unified-content-launcher`. Used to merge upstream commits, resolve conflicts, compile, and run quality gates. Merged into customization branch only upon passing all gates. Deleted after integration. |
| `backup/pre-upstream-sync-YYYYMMDD` | **Pre-Sync Recovery Snapshot** | Local safety tag/branch created prior to any merge operation to guarantee instantaneous rollback capability. |

---

## 2. Remote Architecture & Safety Constraints

- **`origin`** -> Points strictly to your own repository (`https://github.com/jayasuriya-it21/cleanyourtv.git`).
- **`upstream`** -> Points strictly to the original project (`https://github.com/Gathin23/cleanyourtv.git`).
- **Push Protection**: The upstream push URL is permanently disabled (`git remote set-url --push upstream DISABLED`).
- **Zero Force-Push Policy**: `git push --force` and `git push --force-with-lease` are strictly forbidden.
- **No Unreviewed Auto-Merges**: Upstream code is never merged directly into the customization branch without isolated testing.
- **No Credentials / Device Data**: Never commit tokens, SSH keys, passwords, TV IP addresses, or signing keystores.

---

## 3. Step-by-Step Repeatable Synchronization Procedure

Execute these steps in PowerShell from the repository root:

### Step 1: Verify Working Tree Cleanliness
```powershell
git status --short
# Must produce no output. Do not proceed if uncommitted work exists.
```

### Step 2: Fetch Remote Updates
```powershell
git fetch origin --prune
git fetch upstream --prune --tags
```

### Step 3: Discover Upstream Default Branch
```powershell
$upstreamHead = (git remote show upstream | Select-String "HEAD branch:").ToString().Split(":")[-1].Trim()
Write-Host "Upstream default branch is: $upstreamHead"
```

### Step 4: Review Divergence and Changed Files
```powershell
git log --oneline HEAD..upstream/$upstreamHead
git diff --stat HEAD..upstream/$upstreamHead
```

### Step 5: Fast-Forward Local `main`
```powershell
git switch main
git merge --ff-only upstream/$upstreamHead
# If fast-forward fails, STOP. Investigate why local main diverged.
git switch feature/unified-content-launcher
```

### Step 6: Create Recovery Snapshot & Integration Branch
```powershell
$today = Get-Date -Format "yyyyMMdd"
git branch "backup/pre-upstream-sync-$today" feature/unified-content-launcher
git switch -c "integration/upstream-$today" feature/unified-content-launcher
```

### Step 7: Perform Non-Final Upstream Merge
```powershell
git merge --no-commit --no-ff "upstream/$upstreamHead"
```

### Step 8: Conflict Analysis and Resolution
- Inspect conflicts: `git status --short`
- Classify conflicts (bug fix vs. customization).
- Preserve all custom launcher features (`ConfigPolicy`, `RowNavigationManager`, `FocusMemory`, `SyntheticContentFactory`, etc.).
- Incorporate upstream security or driver bug fixes.
- If conflicts require architectural compromise or unverified hardware assumptions, abort immediately: `git merge --abort`.

### Step 9: Local Quality Gate Validation
```powershell
# Whitespace and diff hygiene
git diff --check

# Full Android test suite, lint, and debug assembly
Set-Location launcher
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
Set-Location ..
```

### Step 10: Commit Integration Merge
```powershell
$upstreamShort = (git rev-parse --short "upstream/$upstreamHead")
git commit -m "merge: integrate upstream CleanYourTV updates through $upstreamShort"
```

### Step 11: Merge Back to Customization Branch
```powershell
git switch feature/unified-content-launcher
git merge --no-ff "integration/upstream-$today"

# Re-verify build
Set-Location launcher
.\gradlew.bat test
.\gradlew.bat assembleDebug
Set-Location ..
```

### Step 12: Push Only to `origin`
```powershell
# Dry run verification first
git push --dry-run origin main
git push --dry-run origin feature/unified-content-launcher

# Push to your repository
git push origin main
git push origin feature/unified-content-launcher
```

---

## 4. Proposed Future Automation Design (Documentation-Only)

> [!NOTE]
> This proposal outlines a non-destructive GitHub Actions workflow for notification only. No workflow files are activated automatically.

### Architecture
- **Trigger**: Scheduled weekly cron (e.g., Mondays at 04:00 UTC) or manual `workflow_dispatch`.
- **Function**:
  1. Fetches `upstream/main` and compares against `origin/main`.
  2. If new commits exist, creates a GitHub Issue or draft PR in `origin` titled:
     `Upstream Updates Available: <count> new commits`.
  3. Lists commit log and modified files.
- **Safety Boundaries**:
  - **Never** pushes to `upstream`.
  - **Never** automatically merges into `feature/unified-content-launcher`.
  - **Never** force-pushes.
  - Requires explicit human review, isolated integration branching, and physical TV validation.
