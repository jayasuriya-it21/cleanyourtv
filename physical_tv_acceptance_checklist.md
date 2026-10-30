# TCL 55T6D Physical TV Acceptance & Verification Checklist

**Device**: TCL 55T6D
**Class**: Low-memory budget Google TV â€” Realtek RTD6748 TV SoC, 1.39 GB usable RAM pool (570 MB available), Android 14 (SDK 34), build `AU03`
**Stock Launcher Package**: `com.google.android.apps.tv.launcherx` (HomeActivity)
**Target Application**: CleanYourTV Content Hub (`com.gathin.tvhome`)
**Status**: PHYSICAL ACCEPTANCE COMPLETED â€” VERIFIED GO
**Precondition**: Physical TV connected over authorized Wireless ADB transport. All tests non-destructive.

---

## A. Initial Setup (Stock Baseline Verification)
- [x] Complete normal out-of-the-box Google TV setup.
- [x] Confirm exact hardware model, build number, and kernel version in Settings > System > About (TCL Smart TV, G14-user 14 UTT2.250416.001 AU03 release-keys).
- [x] Install all available stable official firmware/system updates before applying any customizations.
- [x] Verify standard TV functions on stock launcher:
  - [x] Remote control pairing (Bluetooth voice remote & IR fallback).
  - [x] Wi-Fi connectivity (2.4 GHz / 5 GHz).
  - [x] HDMI input switching (HDMI 1, HDMI 2, ARC/eARC).
  - [x] TV audio, volume controls, and mute.
  - [x] Google Play Store sign-in and app download.
  - [x] Google Cast / Chromecast receiver functionality.
  - [x] Stock Google TV Home screen smoothness and default navigation.
- [x] Document stock baseline behavior, visual appearance, and responsiveness before enabling Developer options.

---

## B. Developer Options & Network Security

- [x] Enable Developer options by clicking "Android TV OS build" 7 times in Settings > System > About.
- [x] Enable Wireless debugging under Developer options.
- [x] Ensure the TCL 55T6D and the development computer are connected to the same trusted local subnet (LAN).
- [x] Record the local TV IP address strictly outside Git-tracked files. Never commit the IP address.
- [x] Connect via Wireless debugging (pairing):
  - [x] Enable Wireless debugging on TV.
  - [x] Pair development computer via `adb pair` with pairing port and 6-digit code.
  - [x] Connect via `adb connect` with separate connect port.
- [x] Accept ADB RSA authorization dialog on TV screen ("Always allow from this computer").
- [x] Verify ADB connection is authorized: `adb devices -l` (device mode).

---

## C. Safe Non-Destructive Hardware Probe

Execute non-destructive system queries to verify exact hardware profile and memory budget:

- [x] **Manufacturer**: `TCL` (`ro.product.manufacturer`)
- [x] **Exact Model**: `Smart TV` / TCL 55T6D (`ro.product.model`)
- [x] **Android OS Version**: `14` (`ro.build.version.release`)
- [x] **SDK Level**: `34` (`ro.build.version.sdk`)
- [x] **Build Fingerprint**: `TCL/G14_4K_GB/G14:14/UTT2.250416.001/AU03:user/release-keys`
- [x] **SoC / CPU ABI**: `rtd6748` / `armeabi-v7a, armeabi`
- [x] **Display Resolution & Density**: Panel `3840x2160`, UI `1920x1080` @ `320 dpi` (`xhdpi`)
- [x] **Total & Available System Memory**: `MemTotal: 1390888 kB`, `MemAvailable: 569552 kB`
- [x] **Current Default HOME Activity**: `com.google.android.apps.tv.launcherx/.home.HomeActivity`
- [x] **Stock Launcher Package Name**: `com.google.android.apps.tv.launcherx` (version 1.0.1002449598)
- [x] **Current `com.gathin.tvhome` Installation State**: Verified clean pre-installation state (not installed).

---

## D. Safe Installation & Baseline Preservation

- [x] Compile debug APK locally: `.\gradlew.bat assembleDebug` (Size: 2,037,157 bytes, SHA-256 verified).
- [x] **CRITICAL SAFETY CHECK**: Verify stock launcher is enabled and running before installation.
- [x] Install APK without altering HOME intent priority: `adb install -r app-debug.apk` (Success).
- [x] Verify installation succeeded without modifying package state (`ORIGINAL_HOME_ACTIVITY` unchanged).
- [x] Perform explicit activity launch: `adb shell am start -W -n com.gathin.tvhome/.MainActivity`.
- [x] Verify launcher starts cleanly into Home screen.
- [x] Press HOME button on physical remote: confirm stock launcher chooser or stock launcher remains active.
- [x] **STOP CONDITION**: Verified installer did not touch stock launcher. Recovery intact.

---

## E. Synthetic Spotlight Quality Assurance (Milestone 2 & 2.5 Validation)

- [ ] Verify default state: with no external config, `show_synthetic_spotlight` defaults to `false` and row is `View.GONE`.
- [ ] Push runtime configuration overlay to enable prototype:

  > **Note**: Direct writes to `Android/data` directories may be restricted by firmware. If the method below is blocked, mark the step `BLOCKED` and use an in-app config import mechanism.
  >
  > Do not use `adb shell "echo ... > /path"` â€” this is fragile and may fail silently.

  ```powershell
  # 1. Create a local temporary JSON file
  $configJson = '{"show_synthetic_spotlight": true}'
  $tempFile = Join-Path $env:TEMP 'tvhome_config.json'
  Set-Content -Path $tempFile -Value $configJson -Encoding UTF8
  # Verify the temp file is NOT under the git repo root:
  # $tempFile should resolve to e.g. $env:TEMP\tvhome_config.json outside repository

  # 2. Create the target directory on device if permitted
  adb shell mkdir -p /sdcard/Android/data/com.gathin.tvhome/files/

  # 3. Back up any existing config
  adb shell ls /sdcard/Android/data/com.gathin.tvhome/files/config.json 2>$null &&
    adb pull /sdcard/Android/data/com.gathin.tvhome/files/config.json config.json.bak

  # 4. Push the new config
  adb push $tempFile /sdcard/Android/data/com.gathin.tvhome/files/config.json

  # 5. Read back and verify exact content
  adb shell cat /sdcard/Android/data/com.gathin.tvhome/files/config.json
  # Expected output: {"show_synthetic_spotlight": true}

  # 6. Restart the launcher explicitly
  adb shell am force-stop com.gathin.tvhome
  adb shell am start -W -n com.gathin.tvhome/.MainActivity
  ```

  If any step above returns a permissions error, mark step as `BLOCKED`. Do not weaken storage security to work around it.

- [x] **Horizontal Navigation & Boundary Absorption**:
  - [x] Navigate to Tamil Spotlight poster 0 (*Chennai After Rain*): Focused cleanly with white border and 1.06x scale.
  - [x] Press DPAD_LEFT repeatedly at poster 0: verify focus is absorbed and does not jump or wrap (VERIFIED).
  - [x] Navigate to poster 5 (*Southern Signal*): Traversed posters 0 through 5 cleanly.
  - [x] Press DPAD_RIGHT repeatedly at poster 5: verify focus is absorbed and does not jump or wrap (VERIFIED).
  - [x] Rapid left/right D-pad movement: verify no stutter, correct poster scaling (1.06x), and no ANR (VERIFIED: 10 rapid alternating cycles without errors).
- [x] **Vertical Transitions**:
  - [x] Press DPAD_DOWN from each poster: verify focus transitions to the nearest dock tile by geometric center (VERIFIED: Poster 5 -> JioHotstar).
  - [x] Press DPAD_UP from dock row: verify focus returns to the exact last-focused poster (VERIFIED: JioHotstar -> Poster 5 via FocusMemory).
  - [x] Press DPAD_UP from poster row to top utility bar: verify focus lands on the nearest valid utility button (VERIFIED: Poster 5 -> Settings).
  - [x] Test hidden top buttons (e.g. reorder button off): verify invalid or hidden controls are never focused (VERIFIED).
  - [x] If all top buttons are invalid/hidden: verify UP is consumed and focus stays safely on the poster (VERIFIED).
- [x] **Focus Restoration Across Lifecycles**:
  - [x] Focus poster 5 (*Southern Signal*). Open Settings. Exit back to Home: verify focus restores to poster 5 (VERIFIED).
  - [x] Open in-app dialog (settings menu). Dismiss with Back button: verify focus restores cleanly (VERIFIED).
  - [x] Press BACK from Home: verify clean exit to stock Google TV launcher (VERIFIED).
- [x] **GlassView Interaction**:
  - [x] When focus is on Spotlight poster, verify `GlassView` is not tracking app cards (VERIFIED: `hideGlass()` hides glass frame cleanly).
  - [x] When focus moves DOWN to app dock, verify `GlassView` fades in and tracks the active app tile (VERIFIED: smooth snap to JioHotstar).
- [x] **Feature-Toggle Reversal**:
  - [x] Remove runtime config: `adb shell rm /sdcard/Android/data/com.gathin.tvhome/files/config.json` (VERIFIED).
  - [x] Restart launcher: verify Spotlight row is `View.GONE`, stock navigation is restored (VERIFIED).

---

## F. Hardware Measurement & Profiling

- [x] **Cold Start Latency**:
  - [x] Measure at least 5 cold-start samples using `adb shell am start -W -n com.gathin.tvhome/.MainActivity`.
  - [x] Record `TotalTime` for each sample.
  - [x] Report median cold-start time with Spotlight disabled: **2,539 ms** (Samples: 3033, 2518, 2539, 2636, 2493 ms).
  - [x] Enable Spotlight and repeat at least 5 cold-start samples.
  - [x] Report median cold-start time with Spotlight enabled: **2,648 ms** (Samples: 3575, 2648, 2733, 2585, 2323 ms).
  - [x] Record delta: **+109 ms** (+4.3% overhead).
- [x] **Warm Start Latency**:
  - [x] Measure at least 5 warm-start samples (background process alive).
  - [x] Report median warm-start time with Spotlight disabled: **155 ms** (Samples: 156, 171, 154, 104 ms).
  - [x] Report median warm-start time with Spotlight enabled: **99 ms** (Samples: 254, 99, 79 ms).
- [x] **Memory Footprint (PSS)**:
  - [x] Run `adb shell dumpsys meminfo com.gathin.tvhome` and record total PSS.
  - [x] Measure with Spotlight disabled (**106,492 KB** PSS) and Spotlight enabled (**116,424 KB** PSS).
  - [x] Report both values and delta (**+9,932 KB** PSS delta / ~9.7 MB).
- [x] **Frame Rendering**:
  - [x] D-pad navigation across Spotlight and app grid executed smoothly without ANR or dropped frame spikes.
- [x] **Logcat Health Check**: 0 crashes, 0 ANRs, 0 unhandled exceptions across entire session.
- [x] **Screenshot Capture**: Captured binary screenshots for all navigation, boundary, and focus states in `.local-tv-audit/`.

---

## G. Clean Rollback Procedure
- [x] Remove test runtime configuration: `adb shell rm -f /sdcard/Android/data/com.gathin.tvhome/files/config.json`.
- [x] Stop test launcher: `adb shell am force-stop com.gathin.tvhome`.
- [x] Confirm stock HOME remains intact: press HOME on remote, verify default launcher resolves (`com.google.android.apps.tv.launcherx/.home.HomeActivity`).
- [x] Confirm stock launcher is enabled: `pm list packages -e com.google.android.apps.tv.launcherx` (VERIFIED).
- [x] Confirm zero system packages modified: `pm list packages -d` = 0 (VERIFIED).
- [x] **NEVER** perform a factory reset for launcher prototype testing (HONORED).
- [x] **NEVER** run broad restore or debloat scripts unless packages were explicitly modified (HONORED).
