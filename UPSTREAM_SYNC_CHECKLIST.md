# Upstream Synchronization Safety Checklist

Use this checklist during every upstream synchronization cycle. Every box must be checked before merging into the customization branch or pushing to origin.

- [ ] **Working tree clean**: `git status --short` returns zero modified or untracked files before starting.
- [ ] **origin verified as own repository**: `git remote get-url origin` points to `jayasuriya-it21/cleanyourtv.git`.
- [ ] **upstream verified as original repository**: `git remote get-url upstream` points to `Gathin23/cleanyourtv.git`.
- [ ] **upstream fetched**: `git fetch upstream --prune --tags` completed without errors.
- [ ] **upstream changes reviewed**: `git log` and `git diff --stat` inspected for architecture and permission changes.
- [ ] **backup branch created**: `backup/pre-upstream-sync-YYYYMMDD` created at current customization HEAD.
- [ ] **integration branch created**: `integration/upstream-YYYYMMDD` branched from `feature/unified-content-launcher`.
- [ ] **conflicts documented**: Any merge conflicts listed, classified, and resolved with two-way analysis.
- [ ] **tests passed**: `.\gradlew.bat test` passes with zero failures and zero errors.
- [ ] **lint passed**: `.\gradlew.bat lint` passes with zero errors and no unexplained new warnings.
- [ ] **APK built**: `.\gradlew.bat assembleDebug` succeeds cleanly.
- [ ] **permission changes reviewed**: AndroidManifest.xml and service permissions audited for privacy/security impact.
- [ ] **launcher recovery reviewed**: Stock launcher fallback logic and recovery scripts verified intact.
- [ ] **physical retest requirement identified**: Documented whether physical TCL 55T6D testing is required prior to production deployment.
- [ ] **merge approved**: Integration merge approved and committed on the temporary integration branch.
- [ ] **push target confirmed as origin**: Pushing strictly to `origin` (`git push origin main` / `git push origin feature/unified-content-launcher`).
- [ ] **no force push**: Never use `--force` or `--force-with-lease`.
- [ ] **upstream not pushed**: Upstream push URL remains `DISABLED`.
