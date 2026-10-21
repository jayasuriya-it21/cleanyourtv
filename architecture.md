# CleanYourTV Content Hub: Core Architecture

This document defines the architectural blueprint for the CleanYourTV Content Hub (`com.gathin.tvhome`), transforming the lightweight Android TV launcher into a high-performance, local-first, privacy-respecting content hub for Google TV (Target: TCL 55T6D, India region).

---

## 1. Architectural Principles

1. **Local-First & Offline-Resilient**: The launcher shell, installed apps, and cached content must be 100% operational without internet connectivity.
2. **Strict Hardware Efficiency (1 GB RAM Budget)**:
   - Zero real-time visual blur (use static pre-rendered gradients or solid tinted layers).
   - Zero autoplay video feeds in rows.
   - Zero permanent background services or polling daemons.
   - Bounded memory and disk caches for bitmaps with `RGB_565` optimization for opaque card art.
   - **Dynamic Memory Budgeting**: Memory limits must not assume a static heap (e.g. "15% = 16 MB"). Normal cache budgeting must be based strictly on `ActivityManager.getMemoryClass()`. Do not use `getLargeMemoryClass()` unless `largeHeap` is already enabled and independently justified. Do not add `android:largeHeap="true"`. The proposed cache formula is provisional until verified empirically on the physical TV. Cache implementation remains deferred to Milestone 4.
3. **Remote-First D-Pad Navigation**:
   - Focus must never disappear, jump unexpectedly, or become trapped in nested scroll containers.
   - Seamless focus restoration when returning from external OTT apps or dialogs.
4. **Honest Provider Abstraction**:
   - Capabilities (`OPEN_APP`, `OPEN_SEARCH`, `OPEN_TITLE`, `OPEN_PLAYBACK`) must reflect verified reality.
   - Unverified deep links are never assumed or fabricated; fallback to safe app launching is mandatory.
   - **No Fabricated Playback Progress**: Launching an external OTT application or media player (Netflix, Prime, Hotstar, VLC, Kodi, Nova, MX Player) registers the item as **Recently Opened** only. True playback progress is tracked solely for playback occurring inside a launcher-owned Media3 player, a verified player result contract, or an authenticated server API (e.g. Jellyfin).
5. **Strict Content ID Disambiguation**:
   - Metadata IDs (e.g. TMDB ID) and Provider Content IDs are separated into distinct domain entities.
   - A TMDB ID is never passed into a third-party OTT URI unless explicitly verified on the device.

---

## 2. Source Tree Layout & Status

The following tree represents the current and planned architecture. Classes are explicitly labeled as:
- **[EXISTING - original]**: Core baseline classes from the original launcher.
- **[EXISTING - added in Milestone 1]**: Active domain and app discovery classes implemented in Milestone 1.
- **[PLANNED - Milestone N]**: Future classes staged for subsequent implementation phases.

```text
com.gathin.tvhome/
├── MainActivity.java               # [EXISTING - original] Home entry point, D-pad routing, root view orchestration
├── Config.java                     # [EXISTING - original] Configuration & theme loader (config.xml, config.json)
├── AppAdapter.java                 # [EXISTING - original] Launcher adapter (bridges to AppDiscoveryManager)
├── AppLibraryDialog.java           # [EXISTING - original] Sideloaded & all-app picker dialog
├── GlassView.java                  # [EXISTING - original] Smooth dynamic spring-animated focus indicator
├── ClockDream.java                 # [EXISTING - original] DreamService screensaver with burn-in mitigation
├── Order.java                      # [EXISTING - original] Application order persistence
├── LibraryApps.java                # [EXISTING - original] Hidden and sideloaded app persistence
├── Cards.java                      # [EXISTING - original] Third-party module card verification and rendering
├── DockDecoration.java             # [EXISTING - original] RecyclerView item decoration for dock band
├── SkyView.java                    # [EXISTING - original] Time-of-day dynamic background view
│
├── apps/                           # Application Discovery & Management
│   ├── AppDiscoveryManager.java    # [EXISTING - added in Milestone 1] Extracted package manager scanner & pure normalizer
│   ├── InstalledApp.java           # [EXISTING - added in Milestone 1] Immutable app representation
│   └── AppCategory.java            # [EXISTING - added in Milestone 1] TV Native vs Phone-only enum
│
├── content/model/                  # Domain Entities
│   ├── ContentItem.java            # [EXISTING - added in Milestone 1] Universal media item entity
│   ├── ContentRow.java             # [EXISTING - added in Milestone 1] Shelf container model
│   ├── ContentType.java            # [EXISTING - added in Milestone 1] Media classification enum
│   ├── Artwork.java                # [EXISTING - added in Milestone 1] Image references and sizing hints
│   ├── ContentIdentity.java        # [EXISTING - added in Milestone 1] Disambiguated metadata and provider IDs
│   ├── WatchProgress.java          # [EXISTING - added in Milestone 1] Local/server playback position model
│   ├── UserWatchlistEntry.java     # [EXISTING - added in Milestone 1] Local watchlist persistence entry
│   └── ContentSourceResult.java    # [EXISTING - added in Milestone 1] Safe result wrapper for metadata queries
│
├── content/demo/                   # Synthetic Local Content (Prototype)
│   └── SyntheticContentFactory.java # [PLANNED - Milestone 2] Local demonstration spotlight items
│
├── content/ui/                     # Content UI & Shelf Adapters
│   └── ContentRowAdapter.java      # [PLANNED - Milestone 2] Horizontal RecyclerView shelf adapter
│
├── navigation/                     # Navigation State & Memory
│   ├── FocusMemory.java            # [PLANNED - Milestone 2] Focus restoration & target selection
│   └── RowNavigationManager.java   # [PLANNED - Milestone 2] Pure D-pad directional calculations
│
├── hero/                           # Top Hero Showcase
│   ├── HeroState.java              # [PLANNED - Milestone 3] Current hero display state
│   └── HeroController.java         # [PLANNED - Milestone 3] Hero area view orchestrator
│
├── provider/                       # OTT & Media Player Integration
│   ├── Provider.java               # [PLANNED - Milestone 4] Provider entity
│   ├── ProviderCapability.java     # [PLANNED - Milestone 4] Capability enum
│   ├── ProviderLaunchResult.java   # [PLANNED - Milestone 4] Launch diagnostics
│   └── ProviderAdapter.java        # [PLANNED - Milestone 4] Interface for app integrations
│
├── content/source/                 # Metadata Sources
│   └── ContentSource.java          # [PLANNED - Milestone 4] Metadata provider contract
│
├── content/cache/                  # Memory & Disk Caching
│   ├── ImageCache.java             # [PLANNED - Milestone 4] Bounded LRU bitmap cache
│   └── ContentDiskCache.java       # [PLANNED - Milestone 4] Stale-while-revalidate disk cache
│
└── search/                         # Unified TV Search
    └── SearchEngine.java           # [PLANNED - Milestone 5] Local + metadata debounced search
```

---

## 3. Migration Map

To ensure zero regressions, code is refactored incrementally through compatibility bridges without large-scale file moves:

| Existing Class | Current Responsibility | Proposed Evolution | Milestone |
| :--- | :--- | :--- | :---: |
| `AppAdapter.java` | Scans `PackageManager`, sorts apps, binds dock and grid tiles. | Retains tile view holding and binding; delegates all scanning, filtering, and classification to `AppDiscoveryManager`. | Milestone 1 (Completed) |
| `AppLibraryDialog.java` | Scans `CATEGORY_LEANBACK_LAUNCHER` & `CATEGORY_LAUNCHER` for library popup. | Reuses query and normalization engine from `AppDiscoveryManager.normalizeLibrary(...)`; focuses purely on dialog UI. | Milestone 1 / 2 |
| `LibraryApps.java` | Persists user added/removed app package names. | Preserved as-is; consumed by `AppDiscoveryManager`. | Milestone 1 (Completed) |
| `Order.java` | Compares and persists custom app ordering. | Preserved as-is; consumed by `AppDiscoveryManager`. | Milestone 1 (Completed) |
| `MainActivity.java` | Manages root view, clock, greeting, `GlassView`, and grid focus. | Retains lifecycle and navigation; integrates synthetic horizontal shelf in Milestone 2. | Milestone 2 |
| `ClockDream.java` | Drifting screensaver on black. | Preserved; configurable modes (OLED black, local art) added in Milestone 5. | Milestone 5 |

---

## 4. Disambiguated Content Identity Specification

To avoid conflating metadata IDs (e.g. TMDB) with provider-specific content identifiers:

```java
public final class ContentIdentity implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String canonicalId;         // e.g. "tmdb:movie:550"
    public final String metadataSource;      // e.g. "tmdb", "local", "synthetic"
    public final String providerId;          // e.g. "netflix", "hotstar", "prime"
    public final String providerContentId;   // e.g. "80100172" (Provider's native ID)
    public final String providerWebUrl;      // e.g. "https://www.netflix.com/title/80100172"
    public final String candidateDeepLinkUri;// e.g. "nflx://www.netflix.com/title/80100172"
    public final String region;              // e.g. "IN"
    public final long availabilityTimestamp;

    public ContentIdentity(String canonicalId, String metadataSource, String providerId,
                           String providerContentId, String providerWebUrl,
                           String candidateDeepLinkUri, String region,
                           long availabilityTimestamp) {
        this.canonicalId = canonicalId != null ? canonicalId : "";
        this.metadataSource = metadataSource != null ? metadataSource : "";
        this.providerId = providerId != null ? providerId : "";
        this.providerContentId = providerContentId != null ? providerContentId : "";
        this.providerWebUrl = providerWebUrl != null ? providerWebUrl : "";
        this.candidateDeepLinkUri = candidateDeepLinkUri != null ? candidateDeepLinkUri : "";
        this.region = region != null ? region : "IN";
        this.availabilityTimestamp = availabilityTimestamp;
    }
}
```

---

## 5. Key Contracts (Planned for Later Milestones)

### 5.1 ContentSource Contract (Milestone 4)
```java
public interface ContentSource {
    String getSourceId();
    String getDisplayName();
    boolean isAvailable(Context context);
    ContentSourceResult fetchRows(Context context, String language, String region, boolean forceRefresh);
    ContentSourceResult search(Context context, String query, String language);
    ContentItem getDetails(Context context, String contentId);
    void clearCache(Context context);
}
```

### 5.2 ProviderAdapter Contract (Milestone 4)
```java
public interface ProviderAdapter {
    String getProviderId();
    String getDisplayName();
    String getPackageName();
    boolean isInstalled(Context context);
    Set<ProviderCapability> getCapabilities(Context context);
    ProviderLaunchResult openApp(Context context);
    ProviderLaunchResult openSearch(Context context, String query);
    ProviderLaunchResult openTitle(Context context, ContentItem item);
    ProviderLaunchResult openPlayback(Context context, ContentItem item, WatchProgress progress);
}
```

---

## 6. Dynamic Memory & Caching Architecture (Milestone 4)

- **Heap-Relative Normal Budgeting**: Rather than assuming 15% = 16 MB or requesting `largeHeap`, the normal memory class is obtained via:
  ```java
  ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
  int memoryClassMb = am.getMemoryClass();
  int maxCacheMb = Math.max(4, Math.min(16, memoryClassMb / 8));
  ```
- **No largeHeap Assumption**: `android:largeHeap="true"` must not be added. `getLargeMemoryClass()` is strictly avoided unless `largeHeap` is independently justified and profiled on hardware.
- **Provisional Formula**: This formula is provisional and must be verified empirically on the physical TCL 55T6D.
- **Bitmap Format**: Card poster views decode opaque images using `Bitmap.Config.RGB_565` and `inSampleSize` matching view dimensions.
- **Image Cache Isolation**: The image cache implementation is deferred to Milestone 4.

---

## 7. Domain Model Audit (Milestone 1)

1. **Immutability**: All fields in `ContentItem`, `ContentRow`, `ContentIdentity`, `Artwork`, `WatchProgress`, `UserWatchlistEntry`, and `InstalledApp` are declared `public final`.
2. **Defensive Copying**: Collections in `ContentItem.genres`, `ContentRow.items`, `ContentSourceResult.rows`, and `ContentSourceResult.items` are wrapped with `Collections.unmodifiableList(new ArrayList<>(...))`. Direct reference leakage is prevented.
3. **Null Handling**: String and object parameters are checked against null and defaulted to neutral empty strings or empty collections (`""`, `Collections.emptyList()`).
4. **Equality & Hashing**: `equals` and `hashCode` in `InstalledApp` use pure Java Strings (`packageName`, `activityName`), preventing unmocked Android framework exceptions in unit tests. `ContentItem` uses its canonical `id`.
5. **Unknown Duration Safety**: `WatchProgress` enforces `Math.max(0, ...)` on positions and durations. `getPercentComplete()` checks `if (durationMs <= 0) return 0;` ensuring safe behavior when media duration is unknown.
6. **Serializable / Parcelable**: Standard Java `Serializable` is used purely as a marker without Android framework dependencies. No runtime reflection or persistence coupling is introduced.
7. **Unused Model Status**: `UserWatchlistEntry` represents the planned local watchlist domain model and is ready for Milestone 3/4 without runtime overhead.
