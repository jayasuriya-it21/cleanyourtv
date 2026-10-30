# Physical TV Probe Report: TCL 55T6D (Realtek RTD6748)

**Date**: 2026-10-24
**Scope**: Read-only physical probe over authorized Wireless ADB transport
**Safety Status**: PASSED (zero package modifications, zero setting changes, read-only audit)

---

## 1. Device Hardware & Platform Identification

| Parameter | Probed Value | Analysis |
|---|---|---|
| **Manufacturer** | `TCL` | Verified physical manufacturer |
| **Model** | `Smart TV` (Marketing: TCL 55T6D) | Matches expected target device |
| **Product Name** | `G14_4K_GB` | TCL G14 platform |
| **Device / Codename** | `G14` | TCL G14 hardware board |
| **SoC / Platform** | `Realtek rtd6748` (`ro.board.platform`) | Quad-core Realtek RTD6748 TV SoC |
| **CPU ABI List** | `armeabi-v7a, armeabi` | 32-bit ARM runtime environment |
| **Android Version** | `14` (Android 14 / UpsideDownCake) | Meets and exceeds `minSdk 26` baseline |
| **SDK Level** | `34` | Fully compatible with target SDKs |
| **Firmware Build ID** | `G14-user 14 UTT2.250416.001 AU03 release-keys` | Production user release build |
| **Incremental Version** | `AU03` | Production release keys |

---

## 2. Display Characteristics

| Parameter | Probed Value | Notes |
|---|---|---|
| **Physical Display Panel** | `3840x2160` | 4K UHD Physical Resolution |
| **Override / UI Size** | `1920x1080` | Standard Android TV 1080p UI rendering plane |
| **Physical Density** | `320 dpi` (`xhdpi`) | Clean 2.0x density scaling |

---

## 3. Memory & Storage Status

| Metric | Probed Value | Assessment |
|---|---|---|
| **Total Memory (MemTotal)** | `1,390,888 kB` (~1.39 GB accessible) | Carved out of 1.5 GB / 2 GB hardware RAM for VPU/GPU |
| **Available Memory (MemAvailable)** | `569,552 kB` (~570 MB) | Healthy headroom for lightweight Java launcher |
| **Free Memory (MemFree)** | `327,784 kB` (~328 MB) | Free unallocated memory |
| **Data Partition Capacity** | `7.1 GB` | Internal user storage |
| **Data Partition Available** | `5.4 GB` (24% utilized) | Ample storage for launcher APK (~2 MB) |
| **Device Provisioned** | `1` | Fully provisioned |
| **User Setup Complete** | `1` | Not in Setup Wizard |
| **Retail / Demo Mode** | `Disabled` | Normal consumer operating state |

---

## 4. Current Launcher & System HOME Resolution

| Parameter | Probed Value | Status |
|---|---|---|
| **`ORIGINAL_HOME_ACTIVITY`** | `com.google.android.apps.tv.launcherx/.home.HomeActivity` | Factory Google TV Launcher |
| **Priority / Preferred Order** | `priority=2 preferredOrder=0 match=0x108000 isDefault=true` | Factory default home activity |
| **Stock Launcher Package** | `com.google.android.apps.tv.launcherx` | Enabled (`enabled=0` / default) |
| **Stock Launcher Version** | `1.0.1002449598` (code `835645`) | Stock Google TV LauncherX |
| **Associated Dream Package** | `com.google.android.apps.tv.dreamx` | Google TV Ambient / Screensaver |
| **Disabled Packages List** | *None* (0 disabled packages) | Factory clean package state |
| **`com.gathin.tvhome` Status** | *Not installed* | Clean pre-installation state |

---

## 5. Compatibility Verification Summary

- [x] **SDK Level >= 26**: Probed SDK 34 >= 26.
- [x] **CPU ABI Compatible**: Probed `armeabi-v7a` runs standard pure Java Android bytecode cleanly.
- [x] **Launcher Activity Identifiable**: Factory Google TV `com.google.android.apps.tv.launcherx/.home.HomeActivity` clearly resolved and documented.
- [x] **Storage & Memory Adequate**: 5.4 GB storage free, 570 MB available RAM.
- [x] **Stock Launcher State Preserved**: Stock launcher is enabled and active.
- [x] **No Unrelated Package Disabled**: Zero packages in disabled state.
- [x] **Probe Outcome**: **COMPATIBLE â€” SAFE TO PROCEED TO PHASE 5 & 6**.
