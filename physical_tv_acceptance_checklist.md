# TCL 55T6D Physical TV Acceptance & Verification Checklist

**Device**: TCL 55T6D
**Class**: Low-memory budget Google TV — SoC, RAM, Android version, firmware, and stock launcher package are all **unknown pending hardware probe**
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
- [ ] **Connect via legacy network ADB**:
  - In Settings > Developer options, enable **Network debugging** (TCP/IP mode).
  - The TV will display its IP address and port (typically 5555, but read the exact value shown on screen).
  - Run `adb connect <TV_IP>:<PORT_SHOWN_ON_TV>` using the exact address and port the TV displays.
- [ ] **Or connect via Wireless debugging (pairing)**:
  - In Settings > Developer options, enable **Wireless debugging**.
  - Tap **Pair device with pairing code** — the TV shows a pairing address, pairing port, and a six-digit code.
  - Run `adb pair <PAIRING_ADDRESS>:<PAIRING_PORT>` and enter the code when prompted.
  - After pairing succeeds, the TV shows a separate **connection address and port**.
  - Run `adb connect <CONNECTION_ADDRESS>:<CONNECTION_PORT>` to establish the session.
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
- [ ] **SoC / CPU ABI**: `adb shell getprop ro.product.cpu.abi`
- [ ] **Display Resolution & Refresh Rate**: `adb shell dumpsys display | findstr mBaseDisplayInfo`
- [ ] **Display Density**: `adb shell wm density`
- [ ] **Total & Available System Memory**: `adb shell cat /proc/meminfo`
- [ ] **Current Default HOME Activity**: `adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME`
- [ ] **Stock Launcher Package Name**: Record exact package name from the HOME resolution above. Do not assume a specific package before probing.
- [ ] **Current `com.gathin.tvhome` Installation State**: `adb shell pm list packages com.gathin.tvhome`

---

## D. Safe Installation & Baseline Preservation

- [ ] Compile debug APK locally:

  ```powershell
  Set-Location launcher
  .\gradlew.bat assembleDebug
  Set-Location ..
  ```

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

  > **Note**: Direct writes to `Android/data` directories may be restricted by firmware. If the method below is blocked, mark the step `BLOCKED` and use an in-app config import mechanism.
  >
  > Do not use `adb shell "echo ... > /path"` — this is fragile and may fail silently.

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

- [ ] **Horizontal Navigation & Boundary Absorption**:
  - [ ] Navigate to Tamil Spotlight poster 0 (*Chennai After Rain*).
  - [ ] Press DPAD_LEFT repeatedly at poster 0: verify focus is absorbed and does not jump or wrap.
  - [ ] Navigate to poster 5 (*Southern Signal*).
  - [ ] Press DPAD_RIGHT repeatedly at poster 5: verify focus is absorbed and does not jump or wrap.
  - [ ] Rapid left/right D-pad movement: verify no stutter, correct poster scaling (1.06x), and no ANR.
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
  - [ ] When focus is on Spotlight poster, verify `GlassView` is not tracking app cards (expected from `View.GONE` semantics and focus-change listener — pending integrated hardware verification).
  - [ ] When focus moves DOWN to app dock, verify `GlassView` fades in and tracks the active app tile (pending integrated hardware verification).
- [ ] **Feature-Toggle Reversal**:
  - [ ] Remove runtime config: `adb shell rm /sdcard/Android/data/com.gathin.tvhome/files/config.json`.
  - [ ] If a backup was taken, restore it: `adb push config.json.bak /sdcard/Android/data/com.gathin.tvhome/files/config.json`.
  - [ ] Restart launcher: verify Spotlight row is `View.GONE`, stock navigation is restored.

---

## F. Hardware Measurement & Profiling

> **Measurement-first policy**: All performance budgets must be defined from empirical data collected on the physical
> device. No pre-defined pass/fail thresholds are specified here. Report raw medians; set budgets after data exists.
>
> **Immediate failure criteria** (regardless of measurements):
> - Application crash or ANR during any checklist step.
> - Unrecoverable focus loss requiring a device restart.
> - Launcher unexpectedly replacing stock HOME without explicit HOME switching.

- [ ] **Cold Start Latency**:
  - [ ] Measure at least 5 cold-start samples using `adb shell am start -W -n com.gathin.tvhome/.MainActivity`.
  - [ ] Record `TotalTime` for each sample.
  - [ ] Report median cold-start time with Spotlight disabled (default).
  - [ ] Enable Spotlight and repeat at least 5 cold-start samples.
  - [ ] Report median cold-start time with Spotlight enabled.
  - [ ] Record delta (Spotlight enabled − Spotlight disabled).
- [ ] **Warm Start Latency**:
  - [ ] Measure at least 5 warm-start samples (background process alive).
  - [ ] Report median warm-start time with Spotlight disabled and enabled.
- [ ] **Memory Footprint (PSS)**:
  - [ ] Run `adb shell dumpsys meminfo com.gathin.tvhome` and record total PSS.
  - [ ] Measure with Spotlight disabled and Spotlight enabled.
  - [ ] Report both values and delta.
- [ ] **Frame Rendering**:
  - [ ] Reset frame stats: `adb shell dumpsys gfxinfo com.gathin.tvhome reset`.
  - [ ] Perform a 2-minute D-pad stress test across Spotlight and app grid.
  - [ ] Collect frame statistics: `adb shell dumpsys gfxinfo com.gathin.tvhome`.
  - [ ] Record 90th, 95th, and 99th percentile frame rendering times. Do not assert a frame-rate target before hardware data exists.
  - [ ] Record total frames rendered and total janky frames.
- [ ] **Logcat Health Check**: Run `adb shell logcat -d -v time` and filter for `TvHome|AndroidRuntime|FATAL`. Verify 0 crashes, 0 ANRs.
- [ ] **Screenshot Capture**: Capture screenshots of Spotlight focused, dock focused, and dialog states via `adb exec-out screencap -p > screenshot.png`.

---

## G. Clean Rollback Procedure
- [ ] Remove test runtime configuration: `adb shell rm -f /sdcard/Android/data/com.gathin.tvhome/files/config.json`.
- [ ] Stop test launcher: `adb shell am force-stop com.gathin.tvhome`.
- [ ] Confirm stock HOME remains intact: press HOME on remote, verify default launcher resolves.
- [ ] If uninstall is needed: `adb uninstall com.gathin.tvhome`.
- [ ] **NEVER** perform a factory reset for launcher prototype testing.
- [ ] **NEVER** run broad restore or debloat scripts unless packages were explicitly modified.
