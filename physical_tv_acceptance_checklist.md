# TCL 55T6D Physical TV Acceptance & Verification Checklist

**Device**: TCL 55T6D Google TV
**Class**: Low-memory budget Android TV (~1 GB RAM budget, MediaTek MT5895 class)
**Target Application**: CleanYourTV Content Hub (`com.gathin.tvhome`)
**Status**: PENDING PHYSICAL DEVICE ARRIVAL
**Precondition**: Hardware is ordered but not yet delivered. No ADB connection or TV IP may be used until physical setup is complete.

---

## A. Initial Setup (Stock Baseline Verification)
- [ ] Complete normal out-of-the-box Google TV setup.
- [ ] Confirm exact hardware model, build number, and kernel version in Settings > System > About.
- [ ] Install all available stable official firmware/system updates before applying any customizations.
- [ ] Verify standard TV functions on stock launcher:
  - [ ] Remote control pairing (Bluetooth voice remote & IR fallback).
  - [ ] Wi-Fi connectivity (2.4 GHz / 5 GHz).
  - [ ] HDMI input switching (HDMI 1, HDMI 2, ARC/eARC).
  - [ ] TV audio, volume controls, and mute.
  - [ ] Google Play Store sign-in and app download.
  - [ ] Google Cast / Chromecast receiver functionality.
  - [ ] Stock Google TV Home screen smoothness and default navigation.
- [ ] Document stock baseline behavior, visual appearance, and responsiveness before enabling Developer options.

---

## B. Developer Options & Network Security
- [ ] Enable Developer options by clicking "Android TV OS build" 7 times in Settings > System > About.
- [ ] Enable USB / Network debugging (minimum necessary debugging permissions only).
- [ ] Ensure the TCL 55T6D and the development computer are connected to the same trusted local subnet (LAN).
- [ ] Record the local TV IP address **strictly outside Git-tracked files** (e.g., in a local, gitignored file or temporary shell variable). Never commit the IP address.
- [ ] Connect over ADB: `adb connect <TV_IP>:5555`.
- [ ] Accept ADB RSA authorization dialog on TV screen, ensuring "Always allow from this computer" is checked.
- [ ] Verify ADB connection is authorized: `adb devices`.

---

## C. Safe Non-Destructive Hardware Probe
Execute non-destructive system queries to verify exact hardware profile and memory budget:
- [ ] **Manufacturer**: `adb shell getprop ro.product.manufacturer`
- [ ] **Exact Model**: `adb shell getprop ro.product.model`
- [ ] **Android OS Version**: `adb shell getprop ro.build.version.release`
- [ ] **SDK Level**: `adb shell getprop ro.build.version.sdk`
- [ ] **Build Fingerprint**: `adb shell getprop ro.build.fingerprint`
- [ ] **CPU ABI**: `adb shell getprop ro.product.cpu.abi`
- [ ] **Display Resolution & Refresh Rate**: `adb shell dumpsys display | grep -E "mBaseDisplayInfo|real"`
- [ ] **Display Density**: `adb shell wm density`
- [ ] **Total & Available System Memory**: `adb shell cat /proc/meminfo | grep -E "MemTotal|MemAvailable|MemFree"`
- [ ] **Current Default HOME Activity**: `adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME`
- [ ] **Stock Launcher Package Name**: Confirm stock launcher ID (e.g., `com.google.android.apps.tv.launcherx`).
- [ ] **Current `com.gathin.tvhome` Installation State**: `adb shell pm list packages com.gathin.tvhome`

---

## D. Safe Installation & Baseline Preservation
- [ ] Compile debug APK locally: `launcher/gradlew.bat assembleDebug`.
- [ ] **CRITICAL SAFETY CHECK**: Verify stock launcher is enabled and running before installation.
- [ ] Install APK without altering HOME intent priority: `adb install -r launcher/app/build/outputs/apk/debug/app-debug.apk`.
- [ ] Verify installation succeeded without modifying package state.
- [ ] Perform explicit activity launch: `adb shell am start -W -n com.gathin.tvhome/.MainActivity`.
- [ ] Verify launcher starts cleanly into Home screen.
- [ ] Press HOME button on physical remote: confirm stock launcher chooser or stock launcher remains active.
- [ ] **STOP CONDITION**: If the installer would disable or displace the stock launcher, halt immediately and preserve stock launcher recovery.

---

## E. Synthetic Spotlight Quality Assurance (Milestone 2 & 2.5 Validation)
- [ ] Verify default state: with no external config, `show_synthetic_spotlight` defaults to `false` and row is `View.GONE`.
- [ ] Push runtime configuration overlay to enable prototype:
  ```bash
  adb shell "echo '{\"show_synthetic_spotlight\": true}' > /sdcard/Android/data/com.gathin.tvhome/files/config.json"
  adb shell am force-stop com.gathin.tvhome
  adb shell am start -W -n com.gathin.tvhome/.MainActivity
  ```
- [ ] **Horizontal Navigation & Boundary Absorption**:
  - [ ] Navigate to Tamil Spotlight poster 0 (*Chennai After Rain*).
  - [ ] Press DPAD_LEFT repeatedly at poster 0: verify focus is absorbed and does not jump or wrap.
  - [ ] Navigate to poster 5 (*Southern Signal*).
  - [ ] Press DPAD_RIGHT repeatedly at poster 5: verify focus is absorbed and does not jump or wrap.
  - [ ] Rapid left/right D-pad movement: verify zero stutter, smooth scrolling, and correct poster scaling (1.06x).
- [ ] **Vertical Transitions**:
  - [ ] Press DPAD_DOWN from each poster: verify focus transitions to the nearest dock tile by geometric center.
  - [ ] Press DPAD_UP from dock row: verify focus returns to the exact last-focused poster.
  - [ ] Press DPAD_UP from poster row to top utility bar: verify focus lands on the nearest valid utility button.
  - [ ] Test hidden top buttons (e.g. reorder button off): verify invalid or hidden controls are never focused.
  - [ ] If all top buttons are invalid/hidden: verify UP is consumed and focus stays safely on the poster.
- [ ] **Focus Restoration Across Lifecycles**:
  - [ ] Focus poster 2 (*Marina Lights*). Open an app from the dock or search. Exit back to Home: verify focus restores to poster 2.
  - [ ] Open in-app dialog (card prompt or settings menu). Dismiss with Back button: verify focus restores to poster 2.
  - [ ] Open App Library dialog. Close with Back button: verify focus restores cleanly.
- [ ] **GlassView Interaction**:
  - [ ] When focus is on Spotlight poster, verify `GlassView` is completely hidden and does not track app cards.
  - [ ] When focus moves DOWN to app dock, verify `GlassView` smoothly fades in / springs to the active app tile.
- [ ] **Feature-Toggle Reversal**:
  - [ ] Remove runtime config: `adb shell rm /sdcard/Android/data/com.gathin.tvhome/files/config.json`.
  - [ ] Restart launcher: verify Spotlight row is completely GONE, consumes 0 layout space, and stock navigation is restored.

---

## F. Hardware Measurement & Profiling
- [ ] **Cold Start Latency**: Measure 5 cold start samples using `am start -W -n com.gathin.tvhome/.MainActivity` (target: < 600 ms).
- [ ] **Warm Start Latency**: Measure 5 warm resume samples.
- [ ] **Memory Footprint (PSS)**: Measure runtime memory using `adb shell dumpsys meminfo com.gathin.tvhome` (target: PSS < 60 MB).
- [ ] **Frame Rendering & Dropped Frames**:
  - [ ] Reset frame stats: `adb shell dumpsys gfxinfo com.gathin.tvhome reset`.
  - [ ] Perform a continuous 2-minute rapid D-pad stress test across Spotlight and app grid.
  - [ ] Collect frame statistics: `adb shell dumpsys gfxinfo com.gathin.tvhome`.
  - [ ] Record 90th, 95th, and 99th percentile frame rendering times (target: 0 jank spikes > 50 ms).
- [ ] **Logcat Health Check**: Capture filtered logcat: `adb shell "logcat -d -v time | grep -E 'TvHome|AndroidRuntime|FATAL'"` (verify 0 exceptions, 0 leaks).
- [ ] **Screenshot Capture**: Capture 4K/1080p screenshots of Spotlight focused, dock focused, and dialog states via `adb exec-out screencap -p > screenshot.png`.

---

## G. Clean Rollback Procedure
- [ ] Remove test runtime configuration: `adb shell rm -f /sdcard/Android/data/com.gathin.tvhome/files/config.json`.
- [ ] Stop test launcher: `adb shell am force-stop com.gathin.tvhome`.
- [ ] Confirm stock HOME remains intact: press HOME on remote, verify default launcher resolves.
- [ ] If uninstall is needed: `adb uninstall com.gathin.tvhome`.
- [ ] **NEVER** perform a factory reset for launcher prototype testing.
- [ ] **NEVER** run broad restore or debloat scripts unless packages were explicitly modified.
